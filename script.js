import http from 'k6/http';
import { check, sleep } from 'k6';

// Authorized practice target from the Lab 2 handout.
const TARGET_URL = 'https://test.k6.io';

export const options = {
  vus: 5,
  duration: '30s',
};

export default function () {
  const response = http.get(TARGET_URL);
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}

