package com.turtlecoin.mainservice.domain.transaction.dto;
import com.turtlecoin.mainservice.domain.transaction.entity.Transaction;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TransactionRequestDto {
    private String title;
    private String content;
    private String sellerAddress;
    private double price;
    // Double에서 int로 바꿈 Block chain에는 Double 사용 안됨
    private int weight;
    private Long turtleId;
    private Long buyerId;
    private Long sellerId;
    private List<String> transactionTags;
    private boolean auctionFlag;
}
