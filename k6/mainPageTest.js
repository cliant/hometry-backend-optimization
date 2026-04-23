import http from 'k6/http';
import { check, sleep } from 'k6';

const TOKEN = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ1YW15MjJAaGFubWFpbC5uZXQiLCJuaWNrbmFtZSI6IuywqOqwgOyatOu2hOyImCIsImlhdCI6MTc3Njg2NjQ5NSwiZXhwIjoxNzc2ODg4MDk1fQ.YfNA-a7cr7K7TiP3DHKAAGP0K9uKF_oz8ujtC-IeMoo';

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const res = http.get('http://localhost:8080/api?date=20260421', {
    headers: {
      Authorization: TOKEN,
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}