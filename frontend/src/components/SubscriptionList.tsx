/**
 * 구독 목록 컴포넌트
 * 
 * 수학적 구조:
 * - 구독 서비스 목록을 카드 형태로 표시
 * - 각 카드는 구독 정보를 표시 (서비스명, 가격, 결제 주기, 다음 결제일)
 * 
 * 호출 경로: App.tsx 또는 Home.tsx
 *          → SubscriptionList (렌더링)
 *          → useEffect (컴포넌트 마운트 시)
 *          → api.getSubscriptions(userId) (데이터 로드)
 *          → GET /api/subscriptions?userId={id}
 *          → SubscriptionController.getSubscriptions()
 *          → SubscriptionService.getSubscriptionsByUserId(userId)
 *          → List<SubscriptionResponseDto> 반환
 *          → setSubscriptions() (상태 업데이트)
 *          → 리스트 렌더링
 * 
 * 애니메이션:
 * - 컨테이너: opacity 페이드 인 (0 → 1)
 * - 리스트 아이템: stagger 효과로 순차 등장 (delay: index * 0.1)
 * - 카드 hover: scale 1.02
 * - 카드 click: scale 0.98
 */

import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { getSubscriptions } from '../services/api';
import type { SubscriptionResponseDto } from '../types';
import './SubscriptionList.css';

interface SubscriptionListProps {
  userId: number;
  onSubscriptionUpdate?: () => void;
}

export const SubscriptionList: React.FC<SubscriptionListProps> = ({
  userId,
  onSubscriptionUpdate,
}) => {
  const [subscriptions, setSubscriptions] = useState<SubscriptionResponseDto[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 호출 경로: 컴포넌트 마운트 시 또는 userId 변경 시
   *          → useEffect 실행
   *          → fetchSubscriptions() 호출
   *          → api.getSubscriptions(userId)
   *          → GET /api/subscriptions?userId={id}
   */
  useEffect(() => {
    const fetchSubscriptions = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await getSubscriptions(userId);
        setSubscriptions(data);
      } catch (err) {
        console.error('구독 목록 조회 실패', err);
        setError('구독 목록을 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchSubscriptions();
    }
  }, [userId, onSubscriptionUpdate]);

  /**
   * 결제 주기 한글 변환
   * 
   * 수학적 구조:
   * - MONTHLY → "월간"
   * - QUARTERLY → "분기"
   * - YEARLY → "연간"
   */
  const getBillingCycleLabel = (cycle: string): string => {
    switch (cycle) {
      case 'MONTHLY':
        return '월간';
      case 'QUARTERLY':
        return '분기';
      case 'YEARLY':
        return '연간';
      default:
        return cycle;
    }
  };

  /**
   * 가격 포맷팅
   * 
   * 수학적 구조:
   * - 숫자를 천 단위 구분 기호(,) 포함한 문자열로 변환
   * - 소수점 2자리까지 표시
   */
  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(price);
  };

  if (loading) {
    return (
      <div className="subscription-list-loading">
        <p>구독 목록을 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="subscription-list-error">
        <p>{error}</p>
      </div>
    );
  }

  if (subscriptions.length === 0) {
    return (
      <div className="subscription-list-empty">
        <p>등록된 구독 서비스가 없습니다.</p>
      </div>
    );
  }

  return (
    <motion.div
      className="subscription-list"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.3 }}
    >
      <h2 className="subscription-list-title">구독 서비스 목록</h2>
      <div className="subscription-list-container">
        {subscriptions.map((subscription, index) => (
          <motion.div
            key={subscription.id}
            className="subscription-card"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            <div className="subscription-card-header">
              <h3 className="subscription-card-title">{subscription.serviceName}</h3>
              <span className="subscription-card-billing-cycle">
                {getBillingCycleLabel(subscription.billingCycle)}
              </span>
            </div>
            <div className="subscription-card-body">
              <div className="subscription-card-price">
                {formatPrice(subscription.price)}
              </div>
              <div className="subscription-card-dates">
                <div className="subscription-card-date-item">
                  <span className="subscription-card-date-label">결제일:</span>
                  <span className="subscription-card-date-value">{subscription.billingDate}</span>
                </div>
                <div className="subscription-card-date-item">
                  <span className="subscription-card-date-label">다음 결제일:</span>
                  <span className="subscription-card-date-value">{subscription.nextBillingDate}</span>
                </div>
              </div>
            </div>
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
};
