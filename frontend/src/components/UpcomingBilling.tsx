/**
 * 결제 임박 알림 컴포넌트
 * 
 * 수학적 구조:
 * - 결제 임박 구독 서비스 목록을 카드 형태로 표시
 * - 각 카드는 결제일까지 남은 일수를 표시
 * - 조건: today <= nextBillingDate <= today + 3일
 * 
 * 호출 경로: App.tsx 또는 Home.tsx
 *          → UpcomingBilling (렌더링)
 *          → useEffect (컴포넌트 마운트 시)
 *          → api.getUpcomingSubscriptions(userId) (데이터 로드)
 *          → GET /api/subscriptions/upcoming?userId={id}
 *          → SubscriptionController.getUpcomingSubscriptions()
 *          → SubscriptionService.getUpcomingSubscriptions(userId)
 *          → List<SubscriptionResponseDto> 반환
 *          → setUpcomingSubscriptions() (상태 업데이트)
 *          → 리스트 렌더링
 * 
 * 애니메이션:
 * - 컨테이너: 슬라이드 인 애니메이션 (x: -100 → 0, opacity: 0 → 1)
 * - 리스트 아이템: stagger 효과로 순차 등장 (delay: index * 0.1)
 * - 카드 hover: scale 1.02
 * - 카드 click: scale 0.98
 */

import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { getUpcomingSubscriptions } from '../services/api';
import type { SubscriptionResponseDto } from '../types';
import './UpcomingBilling.css';

interface UpcomingBillingProps {
  userId: number;
  onSubscriptionUpdate?: () => void;
}

/**
 * 결제일까지 남은 일수 계산
 * 
 * 수학적 구조:
 * - 입력: nextBillingDate (YYYY-MM-DD 형식 문자열)
 * - 출력: 남은 일수 (정수)
 * - 계산 공식: daysRemaining = nextBillingDate - today
 * - 결과: 0 이상 3 이하 (3일 이내 결제 예정)
 * 
 * 호출 경로: UpcomingBilling 컴포넌트 렌더링 시
 *          → calculateDaysRemaining(nextBillingDate)
 *          → LocalDate 계산
 *          → 일수 반환
 */
const calculateDaysRemaining = (nextBillingDate: string): number => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const billingDate = new Date(nextBillingDate);
  billingDate.setHours(0, 0, 0, 0);
  
  const diffTime = billingDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  return diffDays >= 0 ? diffDays : 0;
};

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

/**
 * 날짜 포맷팅 (YYYY-MM-DD → YYYY년 MM월 DD일)
 * 
 * 수학적 구조:
 * - 입력: "2024-12-15" 형식
 * - 출력: "2024년 12월 15일" 형식
 */
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}년 ${month}월 ${day}일`;
};

export const UpcomingBilling: React.FC<UpcomingBillingProps> = ({
  userId,
  onSubscriptionUpdate,
}) => {
  const [upcomingSubscriptions, setUpcomingSubscriptions] = useState<SubscriptionResponseDto[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 호출 경로: 컴포넌트 마운트 시 또는 userId 변경 시
   *          → useEffect 실행
   *          → fetchUpcomingSubscriptions() 호출
   *          → api.getUpcomingSubscriptions(userId)
   *          → GET /api/subscriptions/upcoming?userId={id}
   */
  useEffect(() => {
    const fetchUpcomingSubscriptions = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await getUpcomingSubscriptions(userId);
        setUpcomingSubscriptions(data);
      } catch (err) {
        console.error('결제 임박 구독 서비스 조회 실패', err);
        setError('결제 임박 구독 서비스를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchUpcomingSubscriptions();
    }
  }, [userId, onSubscriptionUpdate]);

  if (loading) {
    return (
      <div className="upcoming-billing-loading">
        <p>결제 임박 구독 서비스를 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="upcoming-billing-error">
        <p>{error}</p>
      </div>
    );
  }

  if (upcomingSubscriptions.length === 0) {
    return (
      <motion.div
        className="upcoming-billing-empty"
        initial={{ x: -100, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.3 }}
      >
        <p>결제 예정인 구독 서비스가 없습니다.</p>
      </motion.div>
    );
  }

  return (
    <motion.div
      className="upcoming-billing"
      initial={{ x: -100, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ duration: 0.3, ease: 'easeOut' }}
    >
      <h2 className="upcoming-billing-title">결제 임박 알림</h2>
      <div className="upcoming-billing-container">
        {upcomingSubscriptions.map((subscription, index) => {
          const daysRemaining = calculateDaysRemaining(subscription.nextBillingDate);
          
          return (
            <motion.div
              key={subscription.id}
              className="upcoming-billing-card"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              <div className="upcoming-billing-card-header">
                <h3 className="upcoming-billing-card-title">{subscription.serviceName}</h3>
                <span className="upcoming-billing-card-billing-cycle">
                  {getBillingCycleLabel(subscription.billingCycle)}
                </span>
              </div>
              <div className="upcoming-billing-card-body">
                <div className="upcoming-billing-card-price">
                  {formatPrice(subscription.price)}
                </div>
                <div className="upcoming-billing-card-days">
                  <span className="upcoming-billing-card-days-label">결제까지</span>
                  <span className={`upcoming-billing-card-days-value ${daysRemaining === 0 ? 'today' : daysRemaining <= 1 ? 'urgent' : ''}`}>
                    {daysRemaining === 0 ? '오늘' : `${daysRemaining}일`}
                  </span>
                </div>
                <div className="upcoming-billing-card-date">
                  <span className="upcoming-billing-card-date-label">결제일:</span>
                  <span className="upcoming-billing-card-date-value">
                    {formatDate(subscription.nextBillingDate)}
                  </span>
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>
    </motion.div>
  );
};

