source .env
k6 run --env TOKEN=$TOKEN k6/mainPageTest.js

