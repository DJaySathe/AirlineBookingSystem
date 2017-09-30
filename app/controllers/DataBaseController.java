package controllers;
import Util.Message;
import com.typesafe.config.ConfigFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseController {
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(ConfigFactory.load().getString("db.default.url"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void addTripSegments(int id , List<String> segments){
        for(String s:segments) {
            String sql = "INSERT INTO tripSegments(tripId,segment) VALUES(?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, s);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static Boolean isOnHold(int tid,String flightCode){
        String sql = "SELECT id from holds WHERE tid=? and flightCode=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tid);
            pstmt.setString(2, flightCode);
            ResultSet rs  = pstmt.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    public static void holdSeat(int tid,String flightCode){
        String sql = "INSERT INTO holds(tid,flightCode) VALUES(?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tid);
            pstmt.setString(2, flightCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        sql ="UPDATE flights SET seatsAvailable=seatsAvailable-1 where code=?";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1,flightCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void releaseHold(int tid,String flightCode,Boolean isConfirmed){
        String sql = "DELETE FROM holds WHERE tid=? and flightCode=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tid);
            pstmt.setString(2, flightCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if(!isConfirmed) {
            sql = "UPDATE flights SET seatsAvailable=seatsAvailable+1 where code=?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, flightCode);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

    }
    public static int getAvailability(String flightCode){
        int availability=0;
        String sql = "SELECT seatsAvailable FROM flights WHERE code=?";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
                pstmt.setString(1,flightCode);
                ResultSet rs  = pstmt.executeQuery();
                if(rs.next()) {
                    availability=rs.getInt("seatsAvailable");
                }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return availability;
    }

    public static int getLastBookingId(){
        int id=0;
        String sql = "SELECT MAX(tripid) as nextId FROM tripSegments";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            ResultSet rs  = pstmt.executeQuery();
            if(rs.next()) {
                id=rs.getInt("nextId");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return id;
    }
    public static List<Integer> getTrips(){
        ArrayList<Integer> trips=new ArrayList<>();
        String sql = "SELECT DISTINCT tripId FROM tripSegments";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            ResultSet rs  = pstmt.executeQuery();
            while(rs.next()) {
                trips.add(rs.getInt("tripId"));
                }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return trips;
    }

    public static List<String> getSegments(int id){
        ArrayList<String> segments=new ArrayList<>();
        String sql = "SELECT segment FROM tripSegments where tripId=?";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setInt(1,id);
            ResultSet rs  = pstmt.executeQuery();
            while(rs.next()) {
                segments.add(rs.getString("segment"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return segments;
    }

    public static List<String> getOperators(){
        ArrayList<String> operators=new ArrayList<>();
        String sql = "SELECT Code FROM operators";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            ResultSet rs  = pstmt.executeQuery();
            while(rs.next()) {
                operators.add(rs.getString("Code"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return operators;
    }

    public static List<String> getFlights(String operator){
        ArrayList<String> flights=new ArrayList<>();
        String sql = "SELECT code FROM flights where operatorCode=?";
        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1,operator);
            ResultSet rs  = pstmt.executeQuery();
            while(rs.next()) {
                flights.add(rs.getString("code"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return flights;
    }

}

