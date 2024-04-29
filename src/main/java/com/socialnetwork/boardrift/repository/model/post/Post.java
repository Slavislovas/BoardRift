package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.rest.model.post.ReportDto;

import java.util.Date;
import java.util.List;

public interface Post {
    Date getCreationDate();
    List<ReportDto> getReports();
}
