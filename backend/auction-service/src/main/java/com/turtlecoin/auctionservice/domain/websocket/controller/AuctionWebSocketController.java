package com.turtlecoin.auctionservice.domain.websocket.controller;

import com.turtlecoin.auctionservice.domain.auction.facade.RedissonLockFacade;
import com.turtlecoin.auctionservice.domain.auction.service.BidService;
import com.turtlecoin.auctionservice.domain.websocket.dto.BidMessage;
import com.turtlecoin.auctionservice.global.response.ResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuctionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedissonLockFacade redissonLockFacade;
    private final BidService bidService;

    // 클라이언트가 특정 경매에 입찰을 보낼 때 (/pub/auction/{auctionId}/bid)
    @MessageMapping("/auction/{auctionId}/bid")// 해당 경매 참가자들에게만 메시지 전송
    public void handleBid(@DestinationVariable Long auctionId, BidMessage bidMessage) {
        Long userId = bidMessage.getUserId();
        Double bidAmount = bidMessage.getBidAmount();

        // RedissonLockFacade를 사용하여 입찰 처리
        try {
            redissonLockFacade.updateBidWithLock(auctionId, userId, bidAmount);

            // 성공적으로 입찰이 처리되면 모든 구독자에게 입찰 정보를 전송
            messagingTemplate.convertAndSend("/sub/auction/" + auctionId, bidMessage);
        } catch (Exception e) {
            // 에러 발생 시 에러 메시지를 클라이언트로 전송
            messagingTemplate.convertAndSend("/sub/auction/" + auctionId,
                    ResponseVO.failure("500", "입찰 처리 중 오류가 발생했습니다."));
        }
    }

}
