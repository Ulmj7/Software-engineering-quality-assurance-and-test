import scenario from './script.js';

// This run gives one aggregate summary; use the separate runs for per-level values.
export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m', target: 30 },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 0 },
  ],
};

export default scenario;
