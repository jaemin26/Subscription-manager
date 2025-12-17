/**
 * 메인 애플리케이션 컴포넌트
 * 
 * 수학적 구조:
 * - 모든 구독 서비스 관리 컴포넌트를 통합
 * - 레이아웃 구성: 헤더 → 상단 섹션 (알림 + 차트) → 중앙 섹션 (목록) → 하단 섹션 (폼)
 * 
 * 호출 경로: main.tsx
 *          → App (렌더링)
 *          → 모든 하위 컴포넌트 렌더링:
 *            - UpcomingBilling (결제 임박 알림)
 *            - MonthlyExpenseChart (월별 지출 차트)
 *            - SubscriptionList (구독 목록)
 *            - SubscriptionForm (구독 등록/수정 폼)
 *          → 각 컴포넌트의 useEffect/이벤트 핸들러
 *          → services/api.ts의 API 호출 함수들
 *          → 백엔드 API 엔드포인트
 * 
 * 애니메이션:
 * - 컨테이너: 전체 페이드 인 애니메이션 (opacity: 0 → 1)
 * - 섹션별: 순차 등장 애니메이션 (stagger 효과)
 */

import React, { useState, useCallback } from 'react';
import { motion } from 'framer-motion';
import { SubscriptionList } from './components/SubscriptionList';
import { SubscriptionForm } from './components/SubscriptionForm';
import { UpcomingBilling } from './components/UpcomingBilling';
import { MonthlyExpenseChart } from './components/MonthlyExpenseChart';
import './App.css';

/**
 * 호출 경로: App 컴포넌트 렌더링 시
 *          → useState로 userId 초기화 (임시로 1)
 *          → handleSubscriptionUpdate 콜백 함수 생성
 *          → 각 컴포넌트에 props로 전달
 */
const App: React.FC = () => {
  // 임시로 userId를 1로 설정 (실제 프로젝트에서는 인증 시스템에서 가져옴)
  const [userId] = useState<number>(1);
  const [refreshKey, setRefreshKey] = useState<number>(0);

  /**
   * 호출 경로: SubscriptionForm.onSuccess() 또는 SubscriptionList에서 구독 삭제/수정 후
   *          → handleSubscriptionUpdate() 호출
   *          → setRefreshKey() (상태 업데이트)
   *          → 모든 컴포넌트의 useEffect 의존성 배열에 refreshKey 또는 onSubscriptionUpdate 포함
   *          → 컴포넌트 리렌더링 및 데이터 재조회
   * 
   * 수학적 구조:
   * - refreshKey를 증가시켜 모든 컴포넌트의 데이터를 강제로 갱신
   * - 각 컴포넌트는 onSubscriptionUpdate 콜백을 받아 데이터를 다시 로드
   */
  const handleSubscriptionUpdate = useCallback(() => {
    setRefreshKey((prev) => prev + 1);
  }, []);

  return (
    <motion.div
      className="app"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      {/* 헤더 섹션 */}
      <motion.header
        className="app-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        <h1 className="app-title">나의 구독 서비스 매니저</h1>
        <p className="app-subtitle">정기 결제 서비스를 효율적으로 관리하세요</p>
      </motion.header>

      {/* 메인 컨테이너 */}
      <main className="app-main">
        {/* 상단 섹션: 알림 및 차트 */}
        <motion.section
          className="app-section app-section-top"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          <div className="app-section-grid">
            {/* 결제 임박 알림 */}
            <motion.div
              className="app-section-item"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
            >
              <UpcomingBilling
                userId={userId}
                onSubscriptionUpdate={handleSubscriptionUpdate}
              />
            </motion.div>

            {/* 월별 지출 차트 */}
            <motion.div
              className="app-section-item"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
            >
              <MonthlyExpenseChart
                userId={userId}
                onSubscriptionUpdate={handleSubscriptionUpdate}
              />
            </motion.div>
          </div>
        </motion.section>

        {/* 중앙 섹션: 구독 목록 */}
        <motion.section
          className="app-section app-section-middle"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
        >
          <SubscriptionList
            userId={userId}
            onSubscriptionUpdate={handleSubscriptionUpdate}
          />
        </motion.section>

        {/* 하단 섹션: 구독 등록/수정 폼 */}
        <motion.section
          className="app-section app-section-bottom"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.6 }}
        >
          <SubscriptionForm
            userId={userId}
            onSuccess={handleSubscriptionUpdate}
            onSubscriptionUpdate={handleSubscriptionUpdate}
          />
        </motion.section>
      </main>

      {/* 푸터 섹션 */}
      <motion.footer
        className="app-footer"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5, delay: 0.7 }}
      >
        <p className="app-footer-text">
          © 2024 구독 서비스 매니저. 모든 권리 보유.
        </p>
      </motion.footer>
    </motion.div>
  );
};

export default App;
