package de.daroge.docdemo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.daroge.docdemo.domain.util.NoteValidation;
import de.daroge.docdemo.infrastructure.util.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=DomainConfig.class)
public class NoteTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void test_note_validation() throws JsonProcessingException {
        Note note = new Note(1L,null,null,null,null);
        Validator<NoteValidation> validator = new Validator<>(new NoteValidation());
        NoteValidation container = note.valid(validator);
        String result = objectMapper.writeValueAsString(validator.get());

        NoteValidation expected = new NoteValidation();
        expected.add("title","title is missing");
        expected.add("message","message is missing");
        String expectedResult = objectMapper.writeValueAsString(expected);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
