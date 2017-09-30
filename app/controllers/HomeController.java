package controllers;

import Util.Message;
import Util.Util;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import play.mvc.*;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class HomeController extends Controller {

    // tid would be initialized to last known transaction id from recovery.log
    static int tid=0;
    final static ActorStart actorStart;
    static {
        ActorSystem actorSystem=ActorSystem.create("AirlinesActorSystem");
        actorStart=ActorStart.getInstance();
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public Result bookTrip(String from,String to){
        if(from.equals("X") && to.equals("Y")) {
            final Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
            Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.Book(++tid, from, to), timeout);
            Message.Result result = null;
            try {
                result = (Message.Result) Await.result(future, timeout.duration());
            } catch (Exception e) {

            }
            if(result!=null)
                return created(Util.bookingSucessResponse(result.success, result.tid,result.message));
            else
                return created(Util.bookingSucessResponse(false, tid,"Booking Failed"));
        }else{
            return created(Util.createResponse("Booking from "+from+" to "+to+ " not Supported , Please Try booking X to Y", false));
        }
    }

    public Result getBookedTrips(){
        final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.GetBookedTripList(), timeout);
        Message.Trips trips = null;
        try {
            trips = (Message.Trips)Await.result(future, timeout.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created(Util.trips(trips.tripIds));
    }

    public Result getTripSegments(String tripId){
        final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.GetTripSegments(Integer.parseInt(tripId)), timeout);
        Message.TripSegments tripSegments = null;
        try {
            tripSegments = (Message.TripSegments)Await.result(future, timeout.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created(Util.segments(tripSegments.status,tripSegments.segments));
    }


    public Result getOperatorList(){
        final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.GetOperators(), timeout);
        Message.Operators operatorList=null;
        try {
            operatorList = ( Message.Operators)Await.result(future, timeout.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created(Util.operators(true,operatorList.operators));
    }

    public Result getFlightList(String operator){
        final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.GetFlights(operator), timeout);
        Message.Flights flights=null;
        try {
            flights = (Message.Flights)Await.result(future, timeout.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(flights==null)
            return created(Util.flights(true,new ArrayList<String>()));

        return created(Util.flights(true,flights.flights));
    }
    public   Result   getAvailableSeats(String   operator,   String   flight){
        final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(actorStart.BookingCoordinatorActor, new Message.GetAvailability(operator,flight), timeout);
        int availability=0;
        try {
            availability = (int)Await.result(future, timeout.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created(Util.availability(true,availability));
    }

    public   Result   confirmFail(String   operator){
        actorStart.BookingCoordinatorActor.tell(new Message.ConfirmFail(++tid, operator),ActorRef.noSender());
        return created(Util.createResponse(true));
    }
    public   Result   confirmNoResponse(String   operator){
        actorStart.BookingCoordinatorActor.tell(new Message.NoResponse(++tid, operator),ActorRef.noSender());
        return created(Util.createResponse(true));
    }
    public   Result   reset(String   operator){
        actorStart.BookingCoordinatorActor.tell(new Message.Reset(++tid, operator),ActorRef.noSender());
        return created(Util.createResponse(true));
    }

}
