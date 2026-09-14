import scenario from './script.js';

// Deliberately impossible for this measured external-site baseline.
// Demonstrates a failing quality gate without changing the real SLO.
export const options = {
  vus: 30,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<50'],
    http_req_failed: ['rate<0.01'],
  },
};

export default scenario;
