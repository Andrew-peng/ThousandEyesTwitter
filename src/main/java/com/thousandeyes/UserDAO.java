package com.thousandeyes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbc.JdbcSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Component
public class UserDAO {
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    public BaseUser getUserInfo(String username){
        String idQuery = "SELECT id FROM person WHERE name=:username";
        SqlParameterSource nameParam = new MapSqlParameterSource("username", username);
        
        int id = jdbcTemplate.queryForObject(idQuery,nameParam,Integer.class);
        SqlParameterSource idParam = new MapSqlParameterSource("id", id);

        String followerQuery = "SELECT person.name FROM person JOIN followers ON person.id=followers.follower_person_id AND followers.person_id=:id";
        List<String> followers = jdbcTemplate.queryForList(followerQuery, idParam, String.class);
        
        String followingIDQuery = "SELECT person_id FROM followers WHERE follower_person_id=:id";
        String followingQuery = "SELECT person.name FROM person JOIN followers ON person.id=followers.person_id AND followers.follower_person_id=:id";
        List<Integer> followingID = jdbcTemplate.queryForList(followingIDQuery, idParam, Integer.class);
        List<String> following = jdbcTemplate.queryForList(followingQuery, idParam, String.class);
        
        String messageQuery = "SELECT content FROM tweet WHERE person_id IN (:idlist)";
        List<Integer> newList = new ArrayList<Integer>(followingID);
        newList.add(id);
        Map idsMap = Collections.singletonMap("idlist", newList);
        List<String> messages = jdbcTemplate.queryForList(messageQuery, idsMap, String.class);
        
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setId(id);
        user.setFollowers(followers);
        user.setFollowing(following);
        user.setMessages(messages);
        return user;
    }
    public void follow(int person_id, int follower_person_id) {
        String sql = "INSERT INTO followers (person_id, follower_person_id) VALUES (:person_id, :follower_person_id)";
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("person_id", person_id);
        map.put("follower_person_id", follower_person_id);
        try {
            if (person_id != 0) {
                jdbcTemplate.update(sql, map);
            }
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
        } 
        
    }
    public void unfollow(int person_id, int follower_person_id) {
        String sql = "DELETE FROM followers WHERE person_id=:person_id AND follower_person_id=:follower_person_id";
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("person_id", person_id);
        map.put("follower_person_id", follower_person_id);
        try {
            if (person_id != 0) {
                jdbcTemplate.update(sql, map);
            }
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
        }
    }
    public int getId(String name) {
        String idQuery = "SELECT id FROM person WHERE name=:username";
        SqlParameterSource nameParam = new MapSqlParameterSource("username", name);
        try {
            return jdbcTemplate.queryForObject(idQuery,nameParam,Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
        
    }
}
