/**
 * 구독 등록/수정 폼 컴포넌트
 * 
 * 수학적 구조:
 * - 폼 입력 데이터: SubscriptionRequestDto
 * - 등록 모드: POST /api/subscriptions
 * - 수정 모드: PUT /api/subscriptions/{id}
 * - 제출 시 다음 결제일 자동 계산 (백엔드에서 처리)
 * 
 * 호출 경로: App.tsx 또는 Home.tsx
 *          → SubscriptionForm (렌더링)
 *          → handleSubmit() (폼 제출)
 *          → api.createSubscription() 또는 api.updateSubscription()
 *          → POST /api/subscriptions 또는 PUT /api/subscriptions/{id}
 *          → SubscriptionController.create() 또는 SubscriptionController.update()
 *          → SubscriptionService.createSubscription() 또는 SubscriptionService.updateSubscription()
 *          → SubscriptionResponseDto 반환
 *          → onSuccess() (성공 콜백)
 *          → onSubscriptionUpdate() (부모 컴포넌트 상태 업데이트)
 * 
 * 애니메이션:
 * - 폼 컨테이너: 슬라이드 인 애니메이션 (x: -100 → 0, opacity: 0 → 1)
 * - 제출 버튼 hover: scale 1.02
 * - 제출 버튼 click: scale 0.98
 * - 로딩 스피너: 회전 애니메이션
 * - 성공 메시지: 슬라이드 인 + 페이드 인 (x: 100 → 0, opacity: 0 → 1)
 * - 실패 메시지: 슬라이드 인 + 페이드 인 (x: -100 → 0, opacity: 0 → 1)
 */

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { createSubscription, updateSubscription } from '../services/api';
import type { SubscriptionRequestDto, SubscriptionResponseDto } from '../types';
import { BillingCycle as BillingCycleConst } from '../types';
import './SubscriptionForm.css';

interface SubscriptionFormProps {
  userId: number;
  subscription?: SubscriptionResponseDto | null; // 수정 모드일 때 전달
  onSuccess?: (subscription: SubscriptionResponseDto) => void;
  onCancel?: () => void;
  onSubscriptionUpdate?: () => void;
}

export const SubscriptionForm: React.FC<SubscriptionFormProps> = ({
  userId,
  subscription,
  onSuccess,
  onCancel,
  onSubscriptionUpdate,
}) => {
  const [formData, setFormData] = useState<SubscriptionRequestDto>({
    userId,
    serviceName: '',
    price: 0,
    billingCycle: BillingCycleConst.MONTHLY,
    billingDate: new Date().toISOString().split('T')[0], // 오늘 날짜 기본값
  });

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');

  const isEditMode = !!subscription;

  /**
   * 호출 경로: 컴포넌트 마운트 시 또는 subscription prop 변경 시
   *          → useEffect 실행
   *          → 수정 모드일 경우 폼 데이터 초기화
   */
  useEffect(() => {
    if (subscription) {
      setFormData({
        userId: subscription.userId,
        serviceName: subscription.serviceName,
        price: subscription.price,
        billingCycle: subscription.billingCycle,
        billingDate: subscription.billingDate,
      });
    }
  }, [subscription]);

  /**
   * 폼 입력 필드 변경 핸들러
   * 
   * 호출 경로: 사용자 입력
   *          → onChange 이벤트
   *          → handleInputChange()
   *          → setFormData() (상태 업데이트)
   */
  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'price' ? parseFloat(value) || 0 : value,
    }));
  };

  /**
   * 폼 제출 핸들러
   * 
   * 호출 경로: 폼 제출 버튼 클릭
   *          → handleSubmit()
   *          → setIsSubmitting(true) (로딩 상태)
   *          → api.createSubscription() 또는 api.updateSubscription()
   *          → POST /api/subscriptions 또는 PUT /api/subscriptions/{id}
   *          → SubscriptionController.create() 또는 SubscriptionController.update()
   *          → SubscriptionService.createSubscription() 또는 SubscriptionService.updateSubscription()
   *          → SubscriptionResponseDto 반환
   *          → setSubmitStatus('success') (성공 상태)
   *          → onSuccess() (성공 콜백)
   *          → onSubscriptionUpdate() (부모 컴포넌트 상태 업데이트)
   *          → 또는 setSubmitStatus('error') (실패 상태)
   * 
   * 애니메이션: 제출 시 로딩 스피너 표시, 성공 시 슬라이드 인 애니메이션, 실패 시 슬라이드 인 애니메이션
   */
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitStatus('idle');
    setErrorMessage('');

    try {
      let result: SubscriptionResponseDto;
      
      if (isEditMode && subscription) {
        // 수정 모드
        result = await updateSubscription(subscription.id, formData);
      } else {
        // 등록 모드
        result = await createSubscription(formData);
      }

      setSubmitStatus('success');
      
      // 성공 콜백 호출
      if (onSuccess) {
        onSuccess(result);
      }
      
      // 부모 컴포넌트 상태 업데이트
      if (onSubscriptionUpdate) {
        onSubscriptionUpdate();
      }

      // 등록 모드일 경우 폼 초기화
      if (!isEditMode) {
        setFormData({
          userId,
          serviceName: '',
          price: 0,
          billingCycle: BillingCycleConst.MONTHLY,
          billingDate: new Date().toISOString().split('T')[0],
        });
      }

      // 3초 후 성공 메시지 자동 숨김
      setTimeout(() => {
        setSubmitStatus('idle');
      }, 3000);
    } catch (error) {
      console.error('구독 서비스 저장 실패', error);
      setSubmitStatus('error');
      setErrorMessage(
        error instanceof Error
          ? error.message
          : '구독 서비스를 저장하는데 실패했습니다.'
      );

      // 5초 후 에러 메시지 자동 숨김
      setTimeout(() => {
        setSubmitStatus('idle');
        setErrorMessage('');
      }, 5000);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <motion.div
      className="subscription-form-container"
      initial={{ x: -100, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ duration: 0.3, ease: 'easeOut' }}
    >
      <h2 className="subscription-form-title">
        {isEditMode ? '구독 서비스 수정' : '구독 서비스 등록'}
      </h2>

      <form onSubmit={handleSubmit} className="subscription-form">
        <div className="subscription-form-field">
          <label htmlFor="serviceName" className="subscription-form-label">
            서비스명 <span className="subscription-form-required">*</span>
          </label>
          <input
            type="text"
            id="serviceName"
            name="serviceName"
            value={formData.serviceName}
            onChange={handleInputChange}
            required
            className="subscription-form-input"
            placeholder="예: 넷플릭스, 유튜브 프리미엄"
            disabled={isSubmitting}
          />
        </div>

        <div className="subscription-form-field">
          <label htmlFor="price" className="subscription-form-label">
            가격 (원) <span className="subscription-form-required">*</span>
          </label>
          <input
            type="number"
            id="price"
            name="price"
            value={formData.price}
            onChange={handleInputChange}
            required
            min="0"
            step="0.01"
            className="subscription-form-input"
            placeholder="예: 9500"
            disabled={isSubmitting}
          />
        </div>

        <div className="subscription-form-field">
          <label htmlFor="billingCycle" className="subscription-form-label">
            결제 주기 <span className="subscription-form-required">*</span>
          </label>
          <select
            id="billingCycle"
            name="billingCycle"
            value={formData.billingCycle}
            onChange={handleInputChange}
            required
            className="subscription-form-select"
            disabled={isSubmitting}
          >
            <option value={BillingCycleConst.MONTHLY}>월간</option>
            <option value={BillingCycleConst.QUARTERLY}>분기별</option>
            <option value={BillingCycleConst.YEARLY}>연간</option>
          </select>
        </div>

        <div className="subscription-form-field">
          <label htmlFor="billingDate" className="subscription-form-label">
            결제일 <span className="subscription-form-required">*</span>
          </label>
          <input
            type="date"
            id="billingDate"
            name="billingDate"
            value={formData.billingDate}
            onChange={handleInputChange}
            required
            className="subscription-form-input"
            disabled={isSubmitting}
          />
        </div>

        <div className="subscription-form-actions">
          <motion.button
            type="submit"
            className="subscription-form-submit-button"
            disabled={isSubmitting}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            transition={{ duration: 0.2 }}
          >
            {isSubmitting ? (
              <span className="subscription-form-loading">
                <motion.span
                  className="subscription-form-spinner"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                />
                저장 중...
              </span>
            ) : (
              isEditMode ? '수정하기' : '등록하기'
            )}
          </motion.button>

          {onCancel && (
            <motion.button
              type="button"
              onClick={onCancel}
              className="subscription-form-cancel-button"
              disabled={isSubmitting}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              transition={{ duration: 0.2 }}
            >
              취소
            </motion.button>
          )}
        </div>

        {/* 성공/실패 피드백 메시지 */}
        <AnimatePresence>
          {submitStatus === 'success' && (
            <motion.div
              className="subscription-form-message subscription-form-message-success"
              initial={{ x: 100, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: 100, opacity: 0 }}
              transition={{ duration: 0.3, ease: 'easeOut' }}
            >
              ✓ {isEditMode ? '구독 서비스가 수정되었습니다.' : '구독 서비스가 등록되었습니다.'}
            </motion.div>
          )}

          {submitStatus === 'error' && (
            <motion.div
              className="subscription-form-message subscription-form-message-error"
              initial={{ x: -100, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: -100, opacity: 0 }}
              transition={{ duration: 0.3, ease: 'easeOut' }}
            >
              ✗ {errorMessage || '오류가 발생했습니다.'}
            </motion.div>
          )}
        </AnimatePresence>
      </form>
    </motion.div>
  );
};

