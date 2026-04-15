package com.esports.models;

import java.time.LocalDateTime;

public class Tournament {
    private int id;
    private String name;
    private String game; // varchar(100)
    private String description;
    private String format;
    private int max_teams;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private LocalDateTime registration_deadline;
    private String status;
    private String prize;
    private String rules;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private int organizer_id;

    public Tournament() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public int getMax_teams() { return max_teams; }
    public void setMax_teams(int max_teams) { this.max_teams = max_teams; }
    public LocalDateTime getStart_date() { return start_date; }
    public void setStart_date(LocalDateTime start_date) { this.start_date = start_date; }
    public LocalDateTime getEnd_date() { return end_date; }
    public void setEnd_date(LocalDateTime end_date) { this.end_date = end_date; }
    public LocalDateTime getRegistration_deadline() { return registration_deadline; }
    public void setRegistration_deadline(LocalDateTime registration_deadline) { this.registration_deadline = registration_deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrize() { return prize; }
    public void setPrize(String prize) { this.prize = prize; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }
    public int getOrganizer_id() { return organizer_id; }
    public void setOrganizer_id(int organizer_id) { this.organizer_id = organizer_id; }
}