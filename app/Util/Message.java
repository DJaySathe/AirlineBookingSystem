package Util;

import java.sql.Timestamp;
import java.util.List;

public class Message {
    static public class Book{

        public final int tid;
        public final String from;
        public final String to;

        public Book(int tid, String from, String to) {
            this.tid = tid;
            this.from = from;
            this.to = to;
        }
    }
    static public class Cancel{
        public final int tid;
        public final String flightNo;
        public final String from;
        public final String to;

        public Cancel(int tid, String flightNo, String from, String to) {
            this.tid = tid;
            this.flightNo = flightNo;
            this.from = from;
            this.to = to;
        }
    }
    static public class Hold{

        public final int tid;
        public final String flightNo;
        public final String from;
        public final String to;

        public Hold(int tid, String flightNo, String from, String to) {
            this.tid = tid;
            this.flightNo = flightNo;
            this.from = from;
            this.to = to;
        }
    }
    static public class Confirm{

        public final int tid;
        public final String flightNo;
        public final String from;
        public final String to;

        public Confirm(int tid, String flightNo, String from, String to) {
            this.tid = tid;
            this.flightNo = flightNo;
            this.from = from;
            this.to = to;
        }
    }
    static public class Result{

        public final int tid;
        public final Timestamp validTill;
        public final Boolean success;
        public final String message;

        public Result(int tid, Timestamp validTill, Boolean success, String message) {
            this.tid = tid;
            this.validTill = validTill;
            this.success = success;
            this.message = message;
        }
    }
    static public class GetBookedTripList{

    }
    static public class GetAvailability{
        public final String operator;
        public final String flightNo;


        public GetAvailability(String operator, String flightNo) {
            this.operator = operator;
            this.flightNo = flightNo;
        }
    }
    static public class GetOperators{

    }
    static public class GetFlights{
        public final String operator;

        public GetFlights(String operator) {
            this.operator = operator;
        }
    }

    static public class ConfirmFail{
        public final int tid;
        public final String operator;

        public ConfirmFail(int tid, String operator) {
            this.tid = tid;
            this.operator = operator;
        }
    }
    static public class NoResponse{
        public final int tid;
        public final String operator;

        public NoResponse(int tid, String operator) {
            this.tid = tid;
            this.operator = operator;
        }
    }

    static public class Reset{
        public final int tid;
        public final String operator;

        public Reset(int tid, String operator) {
            this.tid = tid;
            this.operator = operator;
        }
    }
    static public class GetTripSegments{
        public final int id;

        public GetTripSegments(int id) {
            this.id = id;
        }
    }
    static public class Trips{
        public final List<Integer> tripIds;


        public Trips(List<Integer> tripIds) {
            this.tripIds = tripIds;
        }
    }

    static public class Operators{
        public final List<String> operators;


        public Operators(List<String> operators) {
            this.operators = operators;
        }
    }
    static public class Flights{
        public final List<String> flights;


        public Flights(List<String> flights) {
            this.flights = flights;
        }
    }
    static public class TripSegments{
        public final Boolean status;
        public final List<String> segments;

        public TripSegments(Boolean status, List<String> segments) {
            this.status = status;
            this.segments = segments;
        }
    }
}
