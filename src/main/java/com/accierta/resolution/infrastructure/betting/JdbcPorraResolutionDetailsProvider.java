package com.accierta.resolution.infrastructure.betting;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import com.accierta.resolution.domain.port.out.PorraResolutionDetailsProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcPorraResolutionDetailsProvider implements PorraResolutionDetailsProvider {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPorraResolutionDetailsProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PorraResolutionDetails> findById(UUID porraId) {
        List<PorraResolutionDetails> details = jdbcTemplate.query(
                "select * from porra_projection where porra_id = ?",
                (rs, rowNum) -> {
                    List<String> options = jdbcTemplate.query(
                            "select option_value from porra_option_projection where porra_id = ? order by option_value",
                            (ors, idx) -> ors.getString("option_value"),
                            porraId);
                    return new PorraResolutionDetails(
                            rs.getObject("porra_id", UUID.class),
                            rs.getObject("group_id", UUID.class),
                            PorraStatus.valueOf(rs.getString("status")),
                            PorraType.valueOf(rs.getString("type")),
                            options);
                },
                porraId);
        return details.stream().findFirst();
    }
}
