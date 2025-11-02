package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OriginalLogin {
    private Long id;
    private String uuid;
    private Long userId;
    private String source; // e.g. GITHUB
    private String name;   // third-party username / login
}

