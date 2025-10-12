package com.hh.Job.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "book_details" )
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BookDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numberOfPages;

    private String language;

    private Double weight;

    // kick thuoc
    private String dimensions;

    // 🖼️ Ảnh bìa (URL)
    @Column(columnDefinition = "TEXT")
    private String coverImage;

    // 🧠 Mô tả chi tiết thêm (VD: giới thiệu, nội dung tóm tắt)
    @Column(columnDefinition = "MEDIUMTEXT")
    private String additionalInfo;

    // 🔗 Liên kết 1-1 với Book
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"bookDetail"})
    private Book book;

}
