package actors;

import Util.Message;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import controllers.DataBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingCoordinator extends AbstractActor {
    Logger log= LoggerFactory.getLogger("application");

    static int id=DataBaseController.getLastBookingId();

    private final ActorRef AmericanAirlinesActor;
    private final ActorRef BritishAirwaysActor;
    private final ActorRef AirChinaActor;

    public BookingCoordinator(ActorRef AmericanAirlinesActor,ActorRef BritishAirwaysActor,ActorRef AirChinaActor) {
        this.AirChinaActor = AirChinaActor;
        this.AmericanAirlinesActor=AmericanAirlinesActor;
        this.BritishAirwaysActor=BritishAirwaysActor;
    }

    static public Props props(ActorRef AmericanAirlinesActor,ActorRef BritishAirwaysActor,ActorRef AirChinaActor) {
        return Props.create(BookingCoordinator.class, () -> new BookingCoordinator(AmericanAirlinesActor,BritishAirwaysActor,AirChinaActor));
    }

    public void mylog(String message, int tid){
        log.info("[ Transaction Id: " +tid +" ] " + message);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.Book.class, book -> {
                    ++id;
                    ArrayList<String> segmentList=new ArrayList<>();
                    mylog("Trying to book flight from : "+ book.from + " to : "+book.to,book.tid);
                    final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
                    Future<Object> future = Patterns.ask(AirChinaActor, new Message.Hold(book.tid, "CA001",book.from,book.to), timeout);
                    Message.Result resultCA001=null;
                    try {
                        resultCA001 = (Message.Result) Await.result(future, timeout.duration());
                        if(resultCA001.success)
                            mylog("Hold Success on CA001 ", book.tid );
                        else
                            mylog("Hold Failed on CA001 ", book.tid );
                    }catch (Exception e){
                        mylog("Hold Failed due to no response to Hold request from Air China", book.tid );
                    }

                    if(resultCA001.success && (new Timestamp(System.currentTimeMillis())).before(resultCA001.validTill)){
                        future = Patterns.ask(AirChinaActor, new Message.Confirm(book.tid, "CA001",book.from,book.to), timeout);
                        try {
                            resultCA001=(Message.Result) Await.result(future, timeout.duration());
                            if(resultCA001.success) {
                                segmentList.add("CA001");
                                mylog("Confirm Success on CA001", book.tid );
                            } else
                                mylog("Confirm Failed on CA001", book.tid );
                        }catch (Exception e){
                            mylog("Confirm Failed due to no response to Confirm request from Air China", book.tid );
                            resultCA001=new Message.Result(resultCA001.tid,new Timestamp(System.currentTimeMillis()),false, "Confirm Failed due to no response to Confirm request from Air China");
                        }
                    }

                    if(resultCA001.success){
                        DataBaseController.addTripSegments(id,segmentList);
                        mylog("Booking Sucessful From "+book.from +" to " +book.to,book.tid );
                        segmentList=new ArrayList<>();
                        getSender().tell(new Message.Result(id, new Timestamp(System.currentTimeMillis()), resultCA001.success, "Booking Sucessful From "+book.from +" to " +book.to), getSender());
                    }else{
                        future = Patterns.ask(AmericanAirlinesActor, new Message.Hold(book.tid, "AA001",book.from,"Z"), timeout);
                        Message.Result resultAA001=null;
                        try {
                            resultAA001=(Message.Result) Await.result(future, timeout.duration());
                            if(resultAA001.success)
                                mylog("Hold Success on AA001 ", book.tid );
                            else
                                mylog("Hold Failed on AA001 ", book.tid );
                        }catch (Exception e){
                            mylog("Hold Failed due to no response to Hold request from American Airlines", book.tid );
                        }
                        future = Patterns.ask(BritishAirwaysActor, new Message.Hold(book.tid, "BA001","Z",book.to), timeout);

                        Message.Result resultBA001=null;
                        try {
                            resultBA001=(Message.Result) Await.result(future, timeout.duration());
                            if(resultBA001.success)
                                mylog("Hold Success on BA001 ", book.tid );
                            else
                                mylog("Hold Failed on BA001 ", book.tid );
                        }catch (Exception e){
                            mylog("Hold Failed due to no response to Hold request from British Airlines", book.tid );
                        }

                        if(resultAA001.success && resultBA001.success && (new Timestamp(System.currentTimeMillis())).before(resultAA001.validTill) && (new Timestamp(System.currentTimeMillis())).before(resultBA001.validTill)){
                            future = Patterns.ask(AmericanAirlinesActor, new Message.Confirm(book.tid, "AA001",book.from,"Z"), timeout);

                            try {
                                resultAA001=(Message.Result) Await.result(future, timeout.duration());
                                if(resultAA001.success) {
                                    segmentList.add("AA001");
                                    mylog("Confirm Success on AA001", book.tid );
                                } else
                                    mylog("Confirm Failed on AA001", book.tid );
                            }catch (Exception e){
                                mylog("Confirm Failed due to no response to Confirm request from American Airlines", book.tid );
                                resultAA001=new Message.Result(resultAA001.tid,new Timestamp(System.currentTimeMillis()),false, "Confirm Failed due to no response to Confirm request from American Airlines");
                            }
                            future = Patterns.ask(BritishAirwaysActor, new Message.Confirm(book.tid, "BA001","Z",book.to), timeout);
                            try {
                                resultBA001=(Message.Result) Await.result(future, timeout.duration());
                                if(resultBA001.success) {
                                    segmentList.add("BA001");
                                    mylog("Confirm Success on BA001", book.tid );
                                } else
                                    mylog("Confirm Failed on BA001", book.tid );
                            }catch (Exception e){
                                mylog("Confirm Failed due to no response to Confirm request from British Airlines", book.tid );
                                resultBA001=new Message.Result(resultBA001.tid,new Timestamp(System.currentTimeMillis()),false, "Confirm Failed due to no response to Confirm request from British Airlines");
                            }

                        }else{
                            mylog("Cancelling holds on AA001 and BA001 as one of the Hold requests Failed", book.tid );
                            AmericanAirlinesActor.tell(new Message.Cancel(book.tid,"AA001",book.from,"Z"),self());
                            BritishAirwaysActor.tell(new Message.Cancel(book.tid,"BA001","Z",book.to),self());
                        }

                        if(resultAA001.success && resultBA001.success){
                            mylog("Booking Sucessful From "+book.from +" to " +book.to,book.tid );
                            DataBaseController.addTripSegments(id,segmentList);
                            segmentList=new ArrayList<>();
                            getSender().tell(new Message.Result(id, new Timestamp(System.currentTimeMillis()), true, "Booking Sucessful From "+book.from +" to " +book.to), getSender());
                        }else{

                            future = Patterns.ask(AmericanAirlinesActor, new Message.Hold(book.tid, "AA001",book.from,"Z"), timeout);
                            try {
                                resultAA001=(Message.Result) Await.result(future, timeout.duration());
                                if(resultAA001.success)
                                    mylog("Hold Success on AA001 ", book.tid );
                                else
                                    mylog("Hold Failed on AA001 ", book.tid );
                            }catch (Exception e){
                                mylog("Hold Failed due to no response to Hold request from American Airlines", book.tid );
                            }


                            future = Patterns.ask(AirChinaActor, new Message.Hold(book.tid, "CA002","Z","W"), timeout);
                            Message.Result resultCA002=null;
                            try {
                                resultCA002=(Message.Result) Await.result(future, timeout.duration());
                                if(resultCA002.success)
                                    mylog("Hold Success on CA002 ", book.tid );
                                else
                                    mylog("Hold Failed on CA002 ", book.tid );
                            }catch (Exception e){
                                mylog("Hold Failed due to no response to Hold request from Air China", book.tid );
                            }

                            future = Patterns.ask(AmericanAirlinesActor, new Message.Hold(book.tid, "AA002","W",book.to), timeout);
                            Message.Result resultAA002=null;
                            try {
                                resultAA002=(Message.Result) Await.result(future, timeout.duration());
                                if(resultAA002.success)
                                    mylog("Hold Success on AA002 ", book.tid );
                                else
                                    mylog("Hold Failed on AA002 ", book.tid );
                            }catch (Exception e){
                                mylog("Hold Failed due to no response to Hold request from American Airlines", book.tid );
                            }

                            if(resultAA001.success && resultCA002.success && resultAA002.success && (new Timestamp(System.currentTimeMillis())).before(resultAA001.validTill) && (new Timestamp(System.currentTimeMillis())).before(resultCA002.validTill) && (new Timestamp(System.currentTimeMillis())).before(resultAA002.validTill)){
                                future = Patterns.ask(AmericanAirlinesActor, new Message.Confirm(book.tid, "AA001",book.from,"Z"), timeout);
                                try {
                                    resultAA001=(Message.Result) Await.result(future, timeout.duration());
                                    if(resultAA001.success) {
                                        segmentList.add("AA001");
                                        mylog("Confirm Success on AA001", book.tid );
                                    } else
                                        mylog("Confirm Failed on AA001", book.tid );
                                }catch (Exception e){
                                    mylog("Confirm Failed due to no response to Confirm request from American Airlines", book.tid );
                                    resultAA001=new Message.Result(resultAA001.tid,new Timestamp(System.currentTimeMillis()),false, "Confirm Failed due to no response to Confirm request from American Airlines");
                                }


                                future = Patterns.ask(AirChinaActor, new Message.Confirm(book.tid, "CA002","Z","W"), timeout);
                                try {
                                    resultCA002=(Message.Result) Await.result(future, timeout.duration());
                                    if(resultCA002.success) {
                                        segmentList.add("CA002");
                                        mylog("Confirm Success on CA002", book.tid );
                                    } else
                                        mylog("Confirm Failed on CA002", book.tid );
                                }catch (Exception e){
                                    mylog("Confirm Failed due to no response to Confirm request from Air China", book.tid );
                                    resultCA002=new Message.Result(resultCA002.tid,new Timestamp(System.currentTimeMillis()),false, "Confirm Failed due to no response to Confirm request from Air China");
                                }


                                future = Patterns.ask(AmericanAirlinesActor, new Message.Confirm(book.tid, "AA002","W",book.to), timeout);
                                try {
                                    resultAA002=(Message.Result) Await.result(future, timeout.duration());
                                    if(resultAA002.success) {
                                        segmentList.add("AA002");
                                        mylog("Confirm Success on AA002", book.tid );
                                    } else
                                        mylog("Confirm Failed on AA002", book.tid );
                                }catch (Exception e){
                                    mylog("Confirm Failed due to no response to Confirm request from American Airlines", book.tid );
                                    resultAA002=new Message.Result(resultAA002.tid,new Timestamp(System.currentTimeMillis()),false,"Confirm Failed due to no response to Confirm request from American Airlines");
                                }


                                if(resultAA001.success && resultCA002.success && resultAA002.success){
                                    mylog("Booking Sucessful From "+book.from +" to " +book.to,book.tid );
                                    DataBaseController.addTripSegments(id,segmentList);
                                    segmentList=new ArrayList<>();
                                    getSender().tell(new Message.Result(id, new Timestamp(System.currentTimeMillis()), true, "Booking Sucessful From "+book.from +" to " +book.to), getSender());
                                }

                            }else{
                                mylog("Cancelling holds on AA001 ,CA002 and AA002 as one of the Hold requests Failed", book.tid );
                                AmericanAirlinesActor.tell(new Message.Cancel(book.tid,"AA001",book.from,"Z"),self());
                                AirChinaActor.tell(new Message.Cancel(book.tid,"CA002","Z","W"),self());
                                AmericanAirlinesActor.tell(new Message.Cancel(book.tid,"AA002","W",book.to),self());
                                mylog("Booking Failed", book.tid );
                                getSender().tell(new Message.Result(book.tid, new Timestamp(System.currentTimeMillis()), false, "Booking Failed"), getSender());
                            }
                        }

                    }
                    if(segmentList.size()>0){
                        mylog("Partial Booking", book.tid );
                    }


                })
                .match(Message.GetBookedTripList.class, getBookedTripList -> {
                    List<Integer> list=DataBaseController.getTrips();
                    getSender().tell(new Message.Trips(list), getSender());
                })
                .match(Message.GetOperators.class, getOperators -> {
                    List<String> list=DataBaseController.getOperators();
                    getSender().tell(new Message.Operators(list), getSender());
                })
                .match(Message.GetFlights.class, getFlights -> {
                    ActorRef actorRef=AmericanAirlinesActor;
                    if(getFlights.operator.equals("AA")){
                        actorRef=AmericanAirlinesActor;
                    }else if(getFlights.operator.equals("BA")){
                        actorRef=BritishAirwaysActor;
                    }else if(getFlights.operator.equals("CA")){
                        actorRef=AirChinaActor;
                    }
                    final Timeout timeout = new Timeout(1, TimeUnit.SECONDS);
                    Future<Object> future = Patterns.ask(actorRef, getFlights, timeout);
                    Message.Flights flights = null;
                        try {
                            flights = (Message.Flights) Await.result(future, timeout.duration());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getSender().tell(flights, getSender());

                })
                .match(Message.GetAvailability.class, getAvailability -> {
                    ActorRef actorRef=AmericanAirlinesActor;
                    if(getAvailability.operator.equals("AA")){
                        actorRef=AmericanAirlinesActor;
                    }else if(getAvailability.operator.equals("BA")){
                        actorRef=BritishAirwaysActor;
                    }else if(getAvailability.operator.equals("CA")){
                        actorRef=AirChinaActor;
                    }
                    final Timeout timeout = new Timeout(1, TimeUnit.SECONDS);
                    Future<Object> future = Patterns.ask(actorRef, getAvailability, timeout);
                    int availability=0;
                    try {
                        availability = (int)Await.result(future, timeout.duration());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getSender().tell(availability, getSender());

                })
                .match(Message.ConfirmFail.class, confirmFail -> {
                    if(confirmFail.operator.equals("AA")){
                        mylog("Confirm_fail set to True for American Airlines", confirmFail.tid );
                        AmericanAirlinesActor.tell(confirmFail,ActorRef.noSender());
                    }else if(confirmFail.operator.equals("BA")){
                        mylog("Confirm_fail set to True for British Airlines", confirmFail.tid );
                        BritishAirwaysActor.tell(confirmFail,ActorRef.noSender());
                    }else if(confirmFail.operator.equals("CA")){
                        mylog("Confirm_fail set to True for Air China", confirmFail.tid );
                        AirChinaActor.tell(confirmFail,ActorRef.noSender());
                    }

                })
                .match(Message.NoResponse.class, noResponse -> {
                    if(noResponse.operator.equals("AA")){
                        mylog("Confirm_no_response set to True for American Airlines", noResponse.tid );
                        AmericanAirlinesActor.tell(noResponse,ActorRef.noSender());
                    }else if(noResponse.operator.equals("BA")){
                        mylog("Confirm_no_response set to True for British Airlines", noResponse.tid );
                        BritishAirwaysActor.tell(noResponse,ActorRef.noSender());
                    }else if(noResponse.operator.equals("CA")){
                        mylog("Confirm_no_response set to True for Air China", noResponse.tid );
                        AirChinaActor.tell(noResponse,ActorRef.noSender());
                    }

                })
                .match(Message.Reset.class, reset -> {
                    if(reset.operator.equals("AA")){
                        mylog("Confirm_fail and Confirm_no_response set to False for American Airlines", reset.tid );
                        AmericanAirlinesActor.tell(reset,ActorRef.noSender());
                    }else if(reset.operator.equals("BA")){
                        mylog("Confirm_fail and Confirm_no_response set to False for British Airlines", reset.tid );
                        BritishAirwaysActor.tell(reset,ActorRef.noSender());
                    }else if(reset.operator.equals("CA")){
                        mylog("Confirm_fail and Confirm_no_response set to False for Air China", reset.tid );
                        AirChinaActor.tell(reset,ActorRef.noSender());
                    }

                })
                .match(Message.GetTripSegments.class, getTripSegments -> {
                    List<String> list=DataBaseController.getSegments(getTripSegments.id);
                    if(list.size()>0)
                        getSender().tell(new Message.TripSegments(true, list), getSender());
                    else{
                        getSender().tell(new Message.TripSegments(false, list), getSender());
                    }
                })
                .build();
    }
}
