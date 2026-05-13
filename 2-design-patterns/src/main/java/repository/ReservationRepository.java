package repository;

import domain.Reservation;
import java.util.List;

public interface ReservationRepository {
    void save(Reservation reservation);
    List<String> findAll();
}