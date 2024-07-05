package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDao {
    private final JdbcTemplate jdbcTemplate;


    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @Override
//    public List<Transfer> getTransfers() {
//        List<Transfer> transfers = new ArrayList<>();
//        String sql = "SELECT * FROM transfer;";
//        try {
//            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
//            while (results.next()) {
//                transfers.add()
//            }
//        }
//    }


//    private Transfer mapRowToTransfer(SqlRowSet rs) {
//        return transfer;
//    }
}