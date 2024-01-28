window.addEventListener("DOMContentLoaded", function() {
    const script = document.createElement("script");
    script.src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.min.js";
    script.type="module";
    document.head.insertBefore(script, document.getElementsByTagName("script")[0]);

    const selectMarketEl = document.getElementById("select_market");

    let chartData = {};

    const ws = new WebSocket("ws://localhost:3000/upbit");
    ws.onopen = function(e) {

        console.log("연결!", e);
    };

    ws.onclose = function(e) {
        console.log("종료!", e);
    };

    ws.onmessage = function(message) {
        let markets = new Set();
        const items = JSON.parse(message.data);

        const data = items.reduce((acc, cur) => {
            const key = cur.market;
            /*
            acc[cur.market] = acc[cur.market] || {}
            acc[cur.market].tradeDate = cur.trade_date;
            acc[cur.market].tradeTime = cur.trade_time;
            acc[cur.market].accTradePrice = cur.acc_trade_price;
            acc[cur.market].openingPrice = cur.opening_price;
            acc[cur.market].tradePrice = cur.trade_price;
            acc[cur.market].tradeVolume = cur.trade_volume;
            acc[cur.market].highPrice = cur.high_price;
            acc[cur.market].lowPrice = cur.low_price;
            acc[cur.market].changePrice = cur.change_price;
            */
            markets.add(key);

            return acc;
        }, {});

        markets = [...markets];

        const options = selectMarketEl.querySelectorAll("option");
        if (options.length == 0) { // 요소가 없는 경우 최초 한번 탭 생성
            for (let i = 0; i < markets.length; i++) {
                const market = markets[i];

                const option = document.createElement("option");
                const optionText = document.createTextNode(market);
                option.value = market;
                option.appendChild(optionText);

                selectMarketEl.appendChild(option);
                if (i == 0) {
                    chartData = data[market];
                }
            }

        }
        console.log(chartData);
    };


   const ctx = document.getElementById('upbit_cart');
    const labels = ["1월", "2월", "3월", "4월", "5월", "6월", "7월"];
    const data = {
      labels: labels,
      datasets: [{
        label: 'My First Dataset',
        data: [65, 59, 80, 81, 56, 55, 40],
        backgroundColor: [
          'rgba(255, 99, 132, 0.2)',
          'rgba(255, 159, 64, 0.2)',
          'rgba(255, 205, 86, 0.2)',
          'rgba(75, 192, 192, 0.2)',
          'rgba(54, 162, 235, 0.2)',
          'rgba(153, 102, 255, 0.2)',
          'rgba(201, 203, 207, 0.2)'
        ],
        borderColor: [
          'rgb(255, 99, 132)',
          'rgb(255, 159, 64)',
          'rgb(255, 205, 86)',
          'rgb(75, 192, 192)',
          'rgb(54, 162, 235)',
          'rgb(153, 102, 255)',
          'rgb(201, 203, 207)'
        ],
        borderWidth: 1
      }]
    };

    const config = {
      type: 'bar',
      data: data,
      options: {
        scales: {
          y: {
            beginAtZero: true
          }
        }
      },
    };
    new Chart(ctx, config);
});