package org.backend.roko.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class GeminiApiDto {

    // --- 우리가 API에 보낼 요청 형식 ---
    @Getter
    @Setter
    public static class Request {
        private List<Content> contents;

        // 이 생성자 부분을 수정했습니다.
        public Request(String text) {
            Part part = new Part(text);
            Content content = new Content(List.of(part));
            content.setRole("user"); // "role":"user"를 명시적으로 설정
            this.contents = List.of(content);
        }
    }

    // --- API가 우리에게 보내줄 응답 형식 ---
    @Getter
    @Setter
    public static class Response {
        private List<Candidate> candidates;
    }

    @Getter
    @Setter
    public static class Candidate {
        private Content content;
    }

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;
        private String role;

        public Content() {} // 기본 생성자
        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    @Getter
    @Setter
    public static class Part {
        private String text;

        public Part() {} // 기본 생성자
        public Part(String text) {
            this.text = text;
        }
    }
}
