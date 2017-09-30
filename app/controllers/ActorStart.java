package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import play.mvc.Controller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ActorStart extends Controller{

    final ActorRef AmericanAirlinesActor;
    final ActorRef BritishAirwaysActor;
    final ActorRef AirChinaActor;
    final ActorRef BookingCoordinatorActor;

    private static final ActorStart instance=new ActorStart();

    public static ActorStart getInstance(){
        return instance;
    }

    @Inject
    private ActorStart() {
        ActorSystem actorSystem=ActorSystem.create("AirlinesActorSystem");
        AmericanAirlinesActor = actorSystem.actorOf(AmericanAirlines.props());
        BritishAirwaysActor = actorSystem.actorOf(BritishAirways.props(), "BritishAirwaysActor");
        AirChinaActor = actorSystem.actorOf(AirChina.props(), "AirChinaActor");
        BookingCoordinatorActor = actorSystem.actorOf(BookingCoordinator.props(AmericanAirlinesActor, BritishAirwaysActor, AirChinaActor), "BookingCoordinator");

    }

}
