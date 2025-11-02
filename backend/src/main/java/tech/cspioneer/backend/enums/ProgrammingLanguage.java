package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 编程语言枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "编程语言")
public enum ProgrammingLanguage {
    
    @Schema(description = "Java")
    JAVA("JAVA", "Java"),
    
    @Schema(description = "Python")
    PYTHON("PYTHON", "Python"),
    
    @Schema(description = "JavaScript")
    JAVASCRIPT("JAVASCRIPT", "JavaScript"),
    
    @Schema(description = "PHP")
    PHP("PHP", "PHP"),
    
    @Schema(description = "Go")
    GO("GO", "Go"),
    
    @Schema(description = "Rust")
    RUST("RUST", "Rust"),
    
    @Schema(description = "C")
    C("C", "C"),
    
    @Schema(description = "C++")
    CPP("CPP", "C++"),
    
    @Schema(description = "其他")
    OTHER("OTHER", "其他");
    
    @Schema(description = "语言代码")
    private final String code;
    
    @Schema(description = "语言描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static ProgrammingLanguage fromCode(String code) {
        for (ProgrammingLanguage language : ProgrammingLanguage.values()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown programming language code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
