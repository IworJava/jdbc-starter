package com.dmdev.jdbc.starter.dao;

import com.dmdev.jdbc.starter.dto.TicketFilter;
import com.dmdev.jdbc.starter.entity.Ticket;
import com.dmdev.jdbc.starter.exception.DaoException;
import com.dmdev.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketDao implements Dao<Long, Ticket> {
    private static final TicketDao INSTANCE = new TicketDao();
    private static final FlightDao FLIGHT_DAO = FlightDao.getInstance();
    public static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?;
            """;
    public static final String CREATE_SQL = """
            INSERT INTO ticket (passenger_no, passenger_name, flight_id, seat_no, cost)
            VALUES (?, ?, ?, ?, ?);
            """;
    private static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_no = ?,
                passenger_name = ?,
                flight_id = ?,
                seat_no = ?,
                cost = ?
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id,
                passenger_no,
                passenger_name,
                flight_id,
                seat_no,
                cost
            FROM ticket
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + "WHERE id = ?;";

    private TicketDao() {
    }

    public List<Ticket> findAll(TicketFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if (filter.passengerName() != null) {
            parameters.add("%" + filter.passengerName() + "%");
            whereSql.add("passenger_name LIKE ?");
        }
        if (filter.seatNo() != null) {
            parameters.add(filter.seatNo());
            whereSql.add("seat_no = ?");
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());

        String where = "";
        if (!whereSql.isEmpty()) {
            where = whereSql.stream().collect(Collectors.joining(" AND ", " WHERE ", ""));
        }
        where = where.concat(" LIMIT ? OFFSET ?");
        String sql = FIND_ALL_SQL + where;
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            List<Ticket> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(buildTicket(resultSet));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = statement.executeQuery();
            List<Ticket> tickets = new ArrayList<>();

            while (resultSet.next()) {
                Ticket ticket = buildTicket(resultSet);
                tickets.add(ticket);
            }
            return tickets;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Ticket> findById(Long id) {
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            Ticket ticket = null;
            if (resultSet.next()) {
                ticket = buildTicket(resultSet);
            }
            return Optional.ofNullable(ticket);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean update(Ticket ticket) {
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, ticket.getPassengerNo());
            statement.setString(2, ticket.getPassengerName());
            statement.setLong(3, ticket.getFlight().id());
            statement.setString(4, ticket.getSeatNo());
            statement.setBigDecimal(5, ticket.getCost());
            statement.setLong(6, ticket.getId());

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Ticket save(Ticket ticket) {
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ticket.getPassengerNo());
            statement.setString(2, ticket.getPassengerName());
            statement.setLong(3, ticket.getFlight().id());
            statement.setString(4, ticket.getSeatNo());
            statement.setBigDecimal(5, ticket.getCost());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            ticket.setId(generatedKeys.getLong("id"));

            return ticket;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection connection = ConnectionManager.get(); PreparedStatement statement = connection.prepareStatement(DELETE_SQL);) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Ticket buildTicket(ResultSet resultSet) throws SQLException {
        return new Ticket(resultSet.getLong("id"), resultSet.getString("passenger_no"), resultSet.getString("passenger_name"), FLIGHT_DAO.findById(resultSet.getLong("flight_id")).orElse(null), resultSet.getString("seat_no"), resultSet.getBigDecimal("cost"));
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }
}
