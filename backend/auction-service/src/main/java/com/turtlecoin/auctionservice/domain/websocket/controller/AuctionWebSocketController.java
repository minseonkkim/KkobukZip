package com.turtlecoin.auctionservice.domain.websocket.controller;

import com.turtlecoin.auctionservice.domain.auction.entity.Auction;
import com.turtlecoin.auctionservice.domain.auction.facade.RedissonLockFacade;
import com.turtlecoin.auctionservice.domain.auction.repository.AuctionRepository;
import com.turtlecoin.auctionservice.domain.auction.service.BidService;
import com.turtlecoin.auctionservice.domain.websocket.dto.BidMessage;
import com.turtlecoin.auctionservice.feign.MainClient;
import com.turtlecoin.auctionservice.feign.dto.UserResponseDTO;
import com.turtlecoin.auctionservice.global.exception.*;
import com.turtlecoin.auctionservice.global.response.ResponseVO;
import com.turtlecoin.auctionservice.global.utils.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuctionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedissonLockFacade redissonLockFacade;
    private final MainClient mainClient;
    private final RedisTemplate redisTemplate;
    private static final String AUCTION_END_KEY_PREFIX = "auction_end_";
    private static final String AUCTION_BID_KEY = "auction_bid_";
    private final AuctionRepository auctionRepository;
    private final JWTUtil jwtUtil;
    private final BidService bidService;
//    private final BidService bidService;

    @MessageMapping("/auction/{auctionId}/init")
    public void sendInitialData(@DestinationVariable Long auctionId, Principal principal) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new AuctionNotFoundException("경매가 존재하지 않습니다."));
        String bidKey = AUCTION_BID_KEY+auctionId;
        String endKey = AUCTION_END_KEY_PREFIX+auctionId;

        Long userId = Long.valueOf(principal.getName());

        Long remainingTime = redisTemplate.getExpire(endKey, TimeUnit.MILLISECONDS);

        if (!redisTemplate.hasKey(bidKey)) {
            log.warn("Redis에 키가 존재하지 않습니다. 기본값을 사용합니다.");

            // 기본값으로 처리
            Double nowBid = 0D; // 기본값
            Double nextBid = auction.getMinBid();


            // 필요한 데이터를 초기화 (nextBid랑 remainingTime)
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("bidAmount", nowBid);
            initialData.put("nextBid", nextBid);
            initialData.put("remainingTime", remainingTime);

            // 클라이언트에게 데이터 전송
            String destination = "/queue/auction/" + auctionId + "/init";

        // /user/{userId}/queue/auction/{auctionId}/init
            messagingTemplate.convertAndSendToUser(userId.toString(), destination,
                    ResponseVO.bidSuccess("Join", "200", initialData));

            log.info("기본값을 사용하여 유저에게 데이터 전송 완료: userId={}, auctionId={}", userId, auctionId);
        } else {
            // 키가 있는 경우, Redis에서 값을 가져옵니다.
            log.info("Redis에 키가 존재합니다. Redis에서 정보를 가져옵니다.");

            Object bidAmountObj = redisTemplate.opsForHash().get(bidKey, "bidAmount");
            Double nowBid = (bidAmountObj != null) ? Double.parseDouble(bidAmountObj.toString()) : 0D;

            Double nextBid = nowBid + bidService.calculateBidIncrement(nowBid);

            // 필요한 데이터 조회 및 응답 처리
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("bidAmount", nowBid);
            initialData.put("nextBid", nextBid);
            initialData.put("remainingTime", remainingTime);

            // 클라이언트에게 데이터 전송
            String destination = "/queue/auction/" + auctionId + "/init";
            messagingTemplate.convertAndSendToUser(userId.toString(), destination,
                    ResponseVO.bidSuccess("Join", "200", initialData));

            log.info("Redis 유저에게 데이터 전송 완료: userId={}, auctionId={}", userId, auctionId);
        }
    }

    // 클라이언트가 특정 경매에 입찰을 보낼 때 (/pub/auction/{auctionId}/bid)
    @MessageMapping("/auction/{auctionId}/bid")
    public void handleBid(@DestinationVariable Long auctionId, BidMessage bidMessage, Principal principal) {
        Long userId = bidMessage.getUserId();
        Double bidAmount = bidMessage.getBidAmount();
//        log.info("Bid Amount: {}", bidAmount);
        Double nextBid = bidMessage.getNextBid();

        Long socketUserId = Long.valueOf(principal.getName());

        log.info("socketUserId : {}", socketUserId);

        try {
            redissonLockFacade.updateBidWithLock(auctionId, userId, nextBid, socketUserId);
            log.info("입찰이 성공적으로 처리되었습니다: auctionId = {}, userId = {}, bidAmount = {}", auctionId, userId, nextBid);
        } catch (SameUserBidException e) {
            sendFailureMessage(socketUserId, auctionId, "400", "자신의 입찰에 재입찰 할 수 없습니다.");
        } catch (WrongBidAmountException e) {
            sendFailureMessage(socketUserId, auctionId, "400", "현재 입찰가보다 낮거나 같은 금액으로 입찰할 수 없습니다.");
        } catch (AuctionTimeNotValidException e) {
            sendFailureMessage(socketUserId, auctionId, "422", "입찰 가능한 시간이 아닙니다.");
        } catch (AuctionAlreadyFinishedException e) {
            sendFailureMessage(socketUserId, auctionId, "400", "이미 종료된 경매입니다.");
        } catch (BidConcurrencyException e) {
            sendFailureMessage(socketUserId, auctionId, "409", "다른 사람이 입찰 중입니다. 잠시 후 다시 시도하세요.");
        } catch (BidNotValidException e) {
            sendFailureMessage(socketUserId, auctionId, "400", "자신의 경매에 입찰할 수 없습니다.");
        } catch (AuctionNotFoundException e) {
            sendFailureMessage(socketUserId, auctionId, "404", "해당 경매를 찾을 수 없습니다.");
        } catch (Exception e) {
            sendFailureMessage(socketUserId, auctionId, "500", "입찰 처리 중 오류가 발생했습니다.");
        }
    }

    private void sendFailureMessage(Long socketUserId, Long auctionId, String errorCode, String message) {
        String destination = "/queue/auction/" + auctionId + "/init";
        messagingTemplate.convertAndSendToUser(socketUserId.toString(), destination,
                ResponseVO.failure("Bid", errorCode, message));
    }


    public void sendNicknameOnConnect(String userId) {
        // 메인 서비스에서 유저 정보를 가져와서 nickname 전송
        UserResponseDTO user = mainClient.getUserById(Long.parseLong(userId));
        if (user != null) {
            messagingTemplate.convertAndSend("/sub/auction/nickname", user.getNickname());
            log.info("닉네임 전송: {}", user.getNickname());
        }
    }

}
