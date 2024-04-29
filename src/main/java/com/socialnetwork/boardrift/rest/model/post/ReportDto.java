package com.socialnetwork.boardrift.rest.model.post;

import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportDto {
    private Long id;
    private String reason;
    private UserRetrievalMinimalDto reporter;
}
