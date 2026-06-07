package com.hotel.dao;

import com.hotel.model.Customer;
import com.hotel.model.enums.Gender;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerDao {

    private static final String BASE_SELECT = """
            SELECT id, name, phone, gender, email, address, created_at, updated_at
            FROM customers
            """;

    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = (rs, rowNum) -> {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setPhone(rs.getString("phone"));
        customer.setGender(Gender.valueOf(rs.getString("gender")));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            customer.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            customer.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return customer;
    };

    private final JdbcTemplate jdbcTemplate;

    public CustomerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findAll(String search) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY name");

        return jdbcTemplate.query(sql.toString(), CUSTOMER_ROW_MAPPER, params.toArray());
    }

    public Optional<Customer> findById(Long id) {
        try {
            Customer customer = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE id = ?",
                    CUSTOMER_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(customer);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Long save(Customer customer) {
        String sql = """
                INSERT INTO customers (name, phone, gender, email, address)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getGender().name());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(Customer customer) {
        String sql = """
                UPDATE customers
                SET name = ?, phone = ?, gender = ?, email = ?, address = ?
                WHERE id = ?
                """;

        return jdbcTemplate.update(
                sql,
                customer.getName(),
                customer.getPhone(),
                customer.getGender().name(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
    }
}
