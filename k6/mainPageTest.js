import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const token = __ENV.TOKEN;

const slowRate = new Rate('slow_requests');

export const options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    'slow_requests': ['rate<0.1'],  // 이상치(300ms 초과) 비율 10% 미만 목표
  },
};

export default function () {
  const res = http.get('http://localhost:8080/api?date=20260423', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  slowRate.add(res.timings.duration > 300);

  sleep(1);
}
