/**
 * 월별 지출 차트 컴포넌트
 * 
 * 수학적 구조:
 * - 월별 총 지출액을 차트로 시각화
 * - 계산 공식: totalMonthlyExpense = Σ(각 구독의 월 환산 금액)
 *   - MONTHLY: price (그대로)
 *   - QUARTERLY: price ÷ 3
 *   - YEARLY: price ÷ 12
 * 
 * 호출 경로: App.tsx 또는 Home.tsx
 *          → MonthlyExpenseChart (렌더링)
 *          → useEffect (컴포넌트 마운트 시)
 *          → api.getMonthlyExpense(userId) (데이터 로드)
 *          → GET /api/subscriptions/monthly-expense?userId={id}
 *          → SubscriptionController.getMonthlyExpense()
 *          → SubscriptionService.getMonthlyExpense(userId)
 *          → MonthlyExpenseDto 반환
 *          → setMonthlyExpense() (상태 업데이트)
 *          → 차트 렌더링
 * 
 * 애니메이션:
 * - 컨테이너: 등장 애니메이션 (opacity: 0 → 1, y: 20 → 0)
 * - 차트: Recharts 내장 애니메이션
 */

import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';
import { getMonthlyExpense } from '../services/api';
import type { MonthlyExpenseDto } from '../types';
import './MonthlyExpenseChart.css';

interface MonthlyExpenseChartProps {
  userId: number;
  onSubscriptionUpdate?: () => void;
}

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
 * 차트 데이터 포맷팅
 * 
 * 수학적 구조:
 * - 입력: totalMonthlyExpense (숫자)
 * - 출력: 차트용 데이터 배열
 * - 현재 월의 지출액을 차트 형식으로 변환
 * 
 * 호출 경로: MonthlyExpenseChart 컴포넌트 렌더링 시
 *          → formatChartData(totalMonthlyExpense)
 *          → 차트 데이터 배열 반환
 */
const formatChartData = (totalMonthlyExpense: number) => {
  const currentDate = new Date();
  const currentMonth = currentDate.getMonth() + 1;
  const currentYear = currentDate.getFullYear();
  const monthLabel = `${currentYear}년 ${currentMonth}월`;

  return [
    {
      month: monthLabel,
      지출액: totalMonthlyExpense,
    },
  ];
};

/**
 * 커스텀 툴팁 컴포넌트
 * 
 * 호출 경로: Recharts Tooltip 컴포넌트
 *          → CustomTooltip (렌더링)
 *          → 포맷된 가격 표시
 */
const CustomTooltip = ({ active, payload }: { active?: boolean; payload?: Array<{ value: number }> }) => {
  if (active && payload && payload.length) {
    return (
      <div className="monthly-expense-chart-tooltip">
        <p className="monthly-expense-chart-tooltip-label">월별 지출액</p>
        <p className="monthly-expense-chart-tooltip-value">{formatPrice(payload[0].value)}</p>
      </div>
    );
  }
  return null;
};

export const MonthlyExpenseChart: React.FC<MonthlyExpenseChartProps> = ({
  userId,
  onSubscriptionUpdate,
}) => {
  const [monthlyExpense, setMonthlyExpense] = useState<MonthlyExpenseDto | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * 호출 경로: 컴포넌트 마운트 시 또는 userId 변경 시
   *          → useEffect 실행
   *          → fetchMonthlyExpense() 호출
   *          → api.getMonthlyExpense(userId)
   *          → GET /api/subscriptions/monthly-expense?userId={id}
   */
  useEffect(() => {
    const fetchMonthlyExpense = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await getMonthlyExpense(userId);
        setMonthlyExpense(data);
      } catch (err) {
        console.error('월별 지출액 조회 실패', err);
        setError('월별 지출액을 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchMonthlyExpense();
    }
  }, [userId, onSubscriptionUpdate]);

  if (loading) {
    return (
      <div className="monthly-expense-chart-loading">
        <p>월별 지출액을 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="monthly-expense-chart-error">
        <p>{error}</p>
      </div>
    );
  }

  if (!monthlyExpense) {
    return (
      <motion.div
        className="monthly-expense-chart-empty"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <p>월별 지출액 데이터가 없습니다.</p>
      </motion.div>
    );
  }

  const chartData = formatChartData(monthlyExpense.totalMonthlyExpense);
  const colors = ['#2563eb'];

  return (
    <motion.div
      className="monthly-expense-chart"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <h2 className="monthly-expense-chart-title">월별 지출액</h2>
      
      <div className="monthly-expense-chart-summary">
        <div className="monthly-expense-chart-summary-value">
          {formatPrice(monthlyExpense.totalMonthlyExpense)}
        </div>
        <div className="monthly-expense-chart-summary-label">현재 월 총 지출액</div>
      </div>

      <div className="monthly-expense-chart-container">
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={chartData}
            margin={{
              top: 20,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
            <XAxis 
              dataKey="month" 
              stroke="#666"
              style={{ fontSize: '0.875rem' }}
            />
            <YAxis 
              stroke="#666"
              style={{ fontSize: '0.875rem' }}
              tickFormatter={(value) => {
                if (value >= 1000000) {
                  return `${(value / 1000000).toFixed(1)}M`;
                } else if (value >= 1000) {
                  return `${(value / 1000).toFixed(0)}K`;
                }
                return value.toString();
              }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <Bar 
              dataKey="지출액" 
              fill="#2563eb"
              radius={[8, 8, 0, 0]}
              animationDuration={1000}
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </motion.div>
  );
};

