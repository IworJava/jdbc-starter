package com.dmdev.jdbc.starter;

import com.dmdev.jdbc.starter.dao.TicketDao;
import com.dmdev.jdbc.starter.dto.TicketFilter;
import com.dmdev.jdbc.starter.entity.Ticket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DaoRunner {
    public static void main(String[] args) {
        TicketFilter filter = new TicketFilter(10, 0, "Иван Иванов", null);
        TicketDao dao = TicketDao.getInstance();
        List<Ticket> tickets = dao.findAll(filter);
        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
    }

    public static void getAndUpdateTest() {
        TicketDao dao = TicketDao.getInstance();
        Optional<Ticket> ticketOpt = dao.findById(2L);
        System.out.println(ticketOpt);

        boolean isUpdated = false;
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setCost(BigDecimal.valueOf(188.88));
            isUpdated = dao.update(ticket);
        }
        System.out.println(isUpdated);
    }

    public static void deleteTest() {
        TicketDao dao = TicketDao.getInstance();
        System.out.println(dao.delete(56L));
    }

    private static void saveTest() {
        Ticket ticket = new Ticket();
        ticket.setPassengerNo("1234567");
        ticket.setPassengerName("Name1");
        ticket.setFlight(null);
        ticket.setSeatNo("3B");
        ticket.setCost(BigDecimal.TEN);

        TicketDao ticketDao = TicketDao.getInstance();
        Ticket savedTicket = ticketDao.save(ticket);
        System.out.println(savedTicket);
    }
}
