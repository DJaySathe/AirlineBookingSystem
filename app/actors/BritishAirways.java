package actors;

import Util.Message;
import akka.actor.AbstractActor;
import akka.actor.Props;
import controllers.DataBaseController;

import java.sql.Timestamp;
import java.util.List;

public class BritishAirways extends AbstractActor {

    static public Props props() {
        return Props.create(BritishAirways.class, () -> new BritishAirways());
    }

    static Boolean fail;
    static Boolean noResponse;
    public BritishAirways() {
        System.out.println("Creating BritishAirways Actor");
        fail=false;
        noResponse=false;
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.Hold.class, hold -> {
                    System.out.println("Trying to Hold flight " +hold.flightNo+ " from : "+ hold.from + " to : "+hold.to);
                    int availability= DataBaseController.getAvailability(hold.flightNo);
                    System.out.println(availability + " Seats available in " + hold.flightNo);
                    if(DataBaseController.isOnHold(hold.tid,hold.flightNo)){
                        System.out.println("Already one seat on hold for this transaction");
                        getSender().tell(new Message.Result(hold.tid, new Timestamp(System.currentTimeMillis()+30000), true, ""), getSender());
                    }else if(availability>0){
                        DataBaseController.holdSeat(hold.tid,hold.flightNo);
                        System.out.println("Hold Sucessful");
                        getSender().tell(new Message.Result(hold.tid, new Timestamp(System.currentTimeMillis()+30000), true, ""), getSender());
                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        if(DataBaseController.isOnHold(hold.tid,hold.flightNo)) {
                                            DataBaseController.releaseHold(hold.tid,hold.flightNo,false);
                                        }
                                    }
                                },
                                30000
                        );
                    }else{
                        System.out.println("Hold Failed");
                        getSender().tell(new Message.Result(hold.tid, new Timestamp(System.currentTimeMillis()), false, ""), getSender());
                    }
                })
                .match(Message.Confirm.class, confirm -> {
                    if(noResponse) {

                    }else if(fail) {
                        DataBaseController.releaseHold(confirm.tid, confirm.flightNo, false);
                        getSender().tell(new Message.Result(confirm.tid, new Timestamp(System.currentTimeMillis()), false, ""), getSender());
                    }else {
                        System.out.println("Trying to Confirm flight " + confirm.flightNo + " from : " + confirm.from + " to : " + confirm.to);
                        int availability = DataBaseController.getAvailability(confirm.flightNo);
                        System.out.println(availability + " Seats available in " + confirm.flightNo);
                        if (DataBaseController.isOnHold(confirm.tid, confirm.flightNo)) {
                            DataBaseController.releaseHold(confirm.tid, confirm.flightNo, true);
                            System.out.println("Confirm Sucessful");
                            getSender().tell(new Message.Result(confirm.tid, new Timestamp(System.currentTimeMillis()), true, ""), getSender());
                        } else {
                            System.out.println("Confirm Failed");
                            getSender().tell(new Message.Result(confirm.tid, new Timestamp(System.currentTimeMillis()), false, ""), getSender());
                        }
                    }
                })
                .match(Message.ConfirmFail.class, confirmFail -> {
                    fail=true;
                })
                .match(Message.NoResponse.class, noResponseMessage -> {
                    noResponse=true;
                })
                .match(Message.Reset.class, reset -> {
                    noResponse=false;
                    fail=false;
                })
                .match(Message.GetFlights.class, getFlights -> {
                    List<String> list = DataBaseController.getFlights(getFlights.operator);
                    getSender().tell(new Message.Flights(list), getSender());
                })
                .match(Message.GetAvailability.class, getAvailability -> {
                    int availability = DataBaseController.getAvailability(getAvailability.flightNo);
                    getSender().tell(availability, getSender());
                })
                .match(Message.Cancel.class, cancel -> {
                    System.out.println("Cancelling hold on flight " +cancel.flightNo+ " from : "+ cancel.from + " to : "+cancel.to);
                    if(DataBaseController.isOnHold(cancel.tid,cancel.flightNo)){
                        DataBaseController.releaseHold(cancel.tid,cancel.flightNo,false);
                        System.out.println("Hold Removed");
                    }
                })
                .build();
    }
}
