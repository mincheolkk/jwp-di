package next.dao;

import core.annotation.Inject;
import core.annotation.Repository;
import core.jdbc.JdbcTemplate;
import core.jdbc.KeyHolder;
import core.jdbc.PreparedStatementCreator;
import core.jdbc.RowMapper;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import next.model.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class AnswerDao {

    private static final Logger logger = LoggerFactory.getLogger(AnswerDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Inject
    public AnswerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Answer insert(Answer answer) {
        String sql = "INSERT INTO ANSWERS (writer, contents, createdDate, questionId) VALUES (?, ?, ?, ?)";
        PreparedStatementCreator psc = con -> {
            PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, answer.getWriter());
            pstmt.setString(2, answer.getContents());
            pstmt.setTimestamp(3, new Timestamp(answer.getTimeFromCreateDate()));
            pstmt.setLong(4, answer.getQuestionId());
            return pstmt;
        };

        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(psc, keyHolder);
        logger.debug("KeyHolder : {}", keyHolder);
        return findById(keyHolder.getId());
    }

    public Answer findById(long answerId) {
        logger.debug("find AnswerId : {}", answerId);
        String sql = "SELECT answerId, writer, contents, createdDate, questionId FROM ANSWERS WHERE answerId = ?";

        RowMapper<Answer> rm = rs -> new Answer(rs.getLong("answerId"), rs.getString("writer"), rs.getString("contents"),
                                                rs.getTimestamp("createdDate"), rs.getLong("questionId"));

        return jdbcTemplate.queryForObject(sql, rm, answerId);
    }

    public List<Answer> findAllByQuestionId(long questionId) {
        String sql = "SELECT answerId, writer, contents, createdDate FROM ANSWERS WHERE questionId = ? "
            + "order by answerId desc";

        RowMapper<Answer> rm = rs -> new Answer(rs.getLong("answerId"), rs.getString("writer"), rs.getString("contents"),
                                                rs.getTimestamp("createdDate"), questionId);

        return jdbcTemplate.query(sql, rm, questionId);
    }

    public void delete(Long answerId) {
        String sql = "DELETE FROM ANSWERS WHERE answerId = ?";
        jdbcTemplate.update(sql, answerId);
    }
}
