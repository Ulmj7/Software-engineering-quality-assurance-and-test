import scenario from './script.js';

// Baseline p95: 225.93 ms (results/baseline-05vu-30s.txt).
// 225.93 * 1.5 = 338.895 ms, rounded to a 339 ms latency budget.
export const options = {
  vus: 30,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<339'],
    http_req_failed: ['rate<0.01'],
  },
};

export default scenario;

