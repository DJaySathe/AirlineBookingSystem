package Util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.List;

public class Util {
    public static ObjectNode createResponse(String message, boolean status) {

        ObjectNode result = Json.newObject();
        if(status)
            result.put("status", "success");
        else
            result.put("status", "error");
        if (message instanceof String) {
            result.put("message", (String) message);
        }
        else {
            result.putPOJO("message",message);
        }
        return result;
    }
    public static ObjectNode createResponse(boolean status) {

        ObjectNode result = Json.newObject();
        if(status)
            result.put("status", "success");
        else
            result.put("status", "error");
        return result;
    }
    public static ObjectNode bookingSucessResponse(boolean status, int tid, String message) {

        ObjectNode result = Json.newObject();
        if(status) {
            result.put("status", "success");
            result.put("tripID", tid);
            result.put("message", message);
        }else{
            result.put("status", "error");
            result.put("message", message);
        }
        return result;
    }

    public static ObjectNode trips(List<Integer> tripids) {

        ObjectNode result = Json.newObject();
        result.put("status", "success");
        result.putPOJO("trips", tripids);
        return result;
    }
    public static ObjectNode segments(boolean status,List<String> segments) {

        ObjectNode result = Json.newObject();
        if(status) {
            result.put("status", "success");
            result.putPOJO("trips", segments);
        }else{
            result.put("status", "error");
            result.put("message", "Unable to find TripId");
        }
        return result;
    }

    public static ObjectNode operators(boolean status,List<String> operators) {

        ObjectNode result = Json.newObject();
        result.put("status", "success");
        result.putPOJO("operators", operators);
        return result;
    }
    public static ObjectNode flights(boolean status,List<String> flights) {

        ObjectNode result = Json.newObject();
        if(flights==null || flights.size()==0){
            result.put("status", "error");
            result.put("message", "Unable to find Flights for the Operator code ");
        }else {
            result.put("status", "success");
            result.putPOJO("operators", flights);
        }
        return result;
    }
    public static ObjectNode availability(boolean status,int availability) {

        ObjectNode result = Json.newObject();
        if(availability ==0){
            result.put("status", "error");
            result.put("message", "No flights available for this Operator and Flight Combination");
        }else {
            result.put("status", "success");
            result.putPOJO("seats", availability);
        }
        return result;
    }
}