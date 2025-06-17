package com.example.selfworkout.dao;

import com.example.selfworkout.model.UserWorkoutExercise;
import com.example.selfworkout.model.Exercise;
import com.example.selfworkout.util.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal; // BigDecimal import edildi
import java.util.ArrayList;
import java.util.List;

/**
 * UserWorkoutExercises tablosu için Data Access Object sınıfı
 * Kullanıcı antrenman egzersiz detaylarını yönetir
 */
public class UserWorkoutExerciseDAO {

    // SQL sorguları
    private static final String INSERT_USER_WORKOUT_EXERCISE =
            "INSERT INTO UserWorkoutExercises (user_workout_id, exercise_id, set_number, reps, weight, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"; // Sütun adları DB şemasına uygun: reps, weight

    private static final String SELECT_BY_ID =
            "SELECT uwe.*, e.name as exercise_name " +
                    "FROM UserWorkoutExercises uwe " +
                    "LEFT JOIN Exercises e ON uwe.exercise_id = e.id " +
                    "WHERE uwe.id = ?";

    private static final String SELECT_BY_USER_WORKOUT_ID =
            "SELECT uwe.*, e.name as exercise_name " +
                    "FROM UserWorkoutExercises uwe " +
                    "LEFT JOIN Exercises e ON uwe.exercise_id = e.id " +
                    "WHERE uwe.user_workout_id = ?";

    private static final String DELETE_USER_WORKOUT_EXERCISE =
            "DELETE FROM UserWorkoutExercises WHERE id = ?";

    /**
     * Yeni kullanıcı antrenman egzersizi ekler
     */
    public UserWorkoutExercise save(UserWorkoutExercise userWorkoutExercise) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER_WORKOUT_EXERCISE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, userWorkoutExercise.getUserWorkoutId());
            statement.setInt(2, userWorkoutExercise.getExerciseId());
            statement.setInt(3, userWorkoutExercise.getSetNumber());
            // DÜZELTİLDİ: modeldeki doğru getter'lar kullanıldı
            statement.setInt(4, userWorkoutExercise.getActualReps());
            statement.setBigDecimal(5, userWorkoutExercise.getActualWeight());
            statement.setString(6, userWorkoutExercise.getNotes());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Kullanıcı antrenman egzersizi ekleme başarısız, hiçbir satır etkilenmedi.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userWorkoutExercise.setId(generatedKeys.getInt(1));
                    System.out.println("✅ Kullanıcı antrenman egzersizi başarıyla eklendi.");
                    return userWorkoutExercise;
                } else {
                    throw new SQLException("Kullanıcı antrenman egzersizi ekleme başarısız, ID alınamadı.");
                }
            }
        }
    }

    /**
     * ID'ye göre kullanıcı antrenman egzersizi bulur
     */
    public UserWorkoutExercise findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUserWorkoutExercise(resultSet);
                }
                return null;
            }
        }
    }

    /**
     * UserWorkout ID'sine göre antrenman egzersizlerini getirir
     */
    public List<UserWorkoutExercise> findByUserWorkoutId(int userWorkoutId) throws SQLException {
        List<UserWorkoutExercise> userWorkoutExercises = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_WORKOUT_ID)) {

            statement.setInt(1, userWorkoutId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    userWorkoutExercises.add(mapResultSetToUserWorkoutExercise(resultSet));
                }
            }
        }

        return userWorkoutExercises;
    }

    /**
     * Kullanıcı antrenman egzersizini siler
     */
    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER_WORKOUT_EXERCISE)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Kullanıcı antrenman egzersizi başarıyla silindi.");
                return true;
            }
            return false;
        }
    }

    /**
     * ResultSet'ten UserWorkoutExercise nesnesini oluşturur
     */
    private UserWorkoutExercise mapResultSetToUserWorkoutExercise(ResultSet resultSet) throws SQLException {
        UserWorkoutExercise userWorkoutExercise = new UserWorkoutExercise();
        userWorkoutExercise.setId(resultSet.getInt("id"));
        userWorkoutExercise.setUserWorkoutId(resultSet.getInt("user_workout_id"));
        userWorkoutExercise.setExerciseId(resultSet.getInt("exercise_id"));
        userWorkoutExercise.setSetNumber(resultSet.getInt("set_number"));
        // DÜZELTİLDİ: modeldeki doğru setter'lar kullanıldı ve sütun adları DB şemasına uygun
        userWorkoutExercise.setActualReps(resultSet.getInt("reps"));
        userWorkoutExercise.setActualWeight(resultSet.getBigDecimal("weight"));
        userWorkoutExercise.setNotes(resultSet.getString("notes"));

        // İlişkili egzersiz objesi
        try {
            String exerciseName = resultSet.getString("exercise_name");
            if (exerciseName != null) {
                Exercise exercise = new Exercise();
                exercise.setId(userWorkoutExercise.getExerciseId());
                exercise.setName(exerciseName);
                userWorkoutExercise.setExercise(exercise);
            }
        } catch (SQLException e) {
            // JOIN yapılmamışsa ignore et
        }

        return userWorkoutExercise;
    }
}