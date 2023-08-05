package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        String route = train.getRoute();

        int boardingStationIndex = route.indexOf(bookTicketEntryDto.getFromStation().toString());
        int destinationStationIndex = route.indexOf(bookTicketEntryDto.getToStation().toString());

        int bookings = 0;

        for (Ticket ticket : train.getBookedTickets()){

            int startIndexOfTicket = route.indexOf(ticket.getFromStation().toString());
            int endIndexOfTicket = route.indexOf(ticket.getToStation().toString());


            if((startIndexOfTicket < destinationStationIndex && startIndexOfTicket >= boardingStationIndex)||
                    (endIndexOfTicket > boardingStationIndex && endIndexOfTicket <= destinationStationIndex)||
                    (startIndexOfTicket <= boardingStationIndex && endIndexOfTicket >= destinationStationIndex)){
                bookings += ticket.getPassengersList().size();
            }
        }

        if (train.getNoOfSeats() - bookings < bookTicketEntryDto.getNoOfSeats()){
            throw  new Exception("Less tickets are available");
        }

        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();

        //if we autowired passenger repository

        List<Passenger> passengerList = new ArrayList<>();
        Ticket ticket = new Ticket();

        //calculating the fare

        for(Integer passengerId : passengerIds){
            Passenger passenger = passengerRepository.findById(passengerId).get();
            passengerList.add(passenger);
        }
        ticket.setPassengersList(passengerList);
        String [] result = route.split(",");

        int startIndex = -1;

        for (int i = 0 ; i < result.length ; i++){
            if (result[i].equals(bookTicketEntryDto.getFromStation().toString())){
                startIndex = i;
                break;
            }
        }

        int endIndex = -1;
        for (int i = 0 ; i < result.length ; i++){
            if (result[i].equals(bookTicketEntryDto.getToStation().toString())){
                endIndex = i;
            }
        }

        if (startIndex == -1 || endIndex == -1){
            throw new Exception("Invalid stations");
        }

        ticket.setTotalFare(300*(endIndex-startIndex));
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        passenger.getBookedTickets().add(ticket);

        //setting the parent reference
        ticket.setTrain(train);

        ticket = ticketRepository.save(ticket);

        //setting in the parent
        train.getBookedTickets().add(ticket);
        Train savedTrain = trainRepository.save(train);
        return ticket.getTicketId();

    }
}
