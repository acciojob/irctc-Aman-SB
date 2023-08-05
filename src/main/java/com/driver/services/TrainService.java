package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        List<Station> stationRoute = trainEntryDto.getStationRoute();
        int size = stationRoute.size();

        String route = "";

        for(int i = 0 ; i < size ; i++){
            if(i == 0){
                route = String.valueOf(stationRoute.get(i));
            }else{
                route =  route + "," + String.valueOf(stationRoute.get(i));
            }
        }

        // dto to entity
        Train train = new Train();
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        int totalSeats = train.getNoOfSeats();

        String route = train.getRoute();

        int boardingStationIndex = route.indexOf(seatAvailabilityEntryDto.getFromStation().toString());
        int destinationStationIndex = route.indexOf(seatAvailabilityEntryDto.getToStation().toString());

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

        int remainingSeats = totalSeats-bookings;
        return remainingSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        String boardingStation = station.toString();

        Train train = trainRepository.findById(trainId).get();
        if (!train.getRoute().contains(boardingStation)){
            throw new Exception("Train is not passing from this station");
        }
        List<Ticket> bookedTickets = train.getBookedTickets();

        int count = 0;

        for (Ticket ticket : bookedTickets){
            if (ticket.getFromStation().toString().equals(boardingStation)){
                count += ticket.getPassengersList().size();
            }
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).get();
        //we have got the age of the oldest person that is travelling that train
        List<Ticket> bookedTickets = train.getBookedTickets();

        int maxAge = 0;

        for (Ticket ticket : bookedTickets){
            List<Passenger> passengerList = ticket.getPassengersList();

            for (Passenger passenger : passengerList){
                maxAge = Math.max(maxAge,passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        int startMin = startTime.getHour()*60 + startTime.getMinute();
        int endMin = endTime.getHour()*60 + endTime.getMinute();
        List<Train> trains = trainRepository.findAll();

        List<Integer> trainIdsList = new ArrayList<>();
        String currentStation = station.toString();

        for (Train train : trains){
            //Once we got the train and train for each Train we need to do this

            String route = train.getRoute();

            String result[] = route.split(",");

            int extraHours = 0;

            for(String st : result){
                if (st.equals(currentStation)){
                    break;
                }
                extraHours ++;
            }

            int totalHours = train.getDepartureTime().getHour()+ extraHours;
            int totalMinutes = train.getDepartureTime().getMinute();

            int time = totalHours*60 + totalMinutes;

            if (time >= startMin && time <= endMin){
                trainIdsList.add(train.getTrainId());
            }
        }
        return trainIdsList;
    }

}
