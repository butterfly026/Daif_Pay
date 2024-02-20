package com.daifubackend.api.controller;

import com.daifubackend.api.service.CacheService;
import com.daifubackend.api.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BaseCacheController extends BaseController{

    @Autowired
    CacheService cacheService;

    public boolean IsLockedProcedure(String lock_key){
        Object obj = cacheService.getCacheEntry("memcached", lock_key);
        if(obj == null)
            return false;
        return obj.toString().equals("lock");
    }

    public void UnlockProcedure(String lock_key) {
        cacheService.clearIndividualCacheEntry("memcached", lock_key);
    }

    public void LockProcedure(String lock_key) {
        cacheService.addNewCacheEntry("memcached", lock_key, "lock");
    }
    public void LockProcedure(String lock_key, int nTime) {
        cacheService.addNewCacheEntry("memcached", lock_key, "lock");
    }

    public boolean isCallbackLocked(String orderId) {
        return IsLockedProcedure(orderId);
    }


    public void rejectUnlock(String orderId) {
        UnlockProcedure("doReject" + orderId);
    }

    public boolean isRejectLocked(String orderId) {
        return IsLockedProcedure("doReject" + orderId);
    }

    public void SetCallbackLock(String orderId, int tim, boolean bl) {
        if(bl) {
            log.info("SetCallbackLock 加锁：{}", orderId);
            LockProcedure(CommonUtils.CALLBACK_LOCK + orderId);
        } else {
            log.info("SetCallbackLock 解锁：{}", orderId);
            UnlockProcedure(CommonUtils.CALLBACK_LOCK + orderId);
        }
    }

    public boolean isDisabledAllPay() {
        Object obj = cacheService.getCacheEntry("memcached", "disable_all_pay");
        if(obj != null && (boolean) obj) {
            return true;
        } else {
            return false;
        }
    }

    public void RejectSetlock(String orderId, int time) {
        LockProcedure("doReject" + orderId, time);
    }
}
