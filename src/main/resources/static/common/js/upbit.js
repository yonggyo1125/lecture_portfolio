window.addEventListener("DOMContentLoaded", async function() {
    const response = await fetch("https://api.coinpaprika.com/v1/tickers?quotes=KRW")
    const json = await response.json();
    const list50 = json.slice(0, 50);
    const labels = list50.map(s => `${s.name}(KRW ${Math.round(s.quotes.KRW.price).toLocaleString()})`);
    const betas = list50.map(s => s.beta_value); // 베타 계수
    const change1H = list50.map(s => s.quotes.KRW.percent_change_1h); // 1시간 간격 등락폭
    const change6H = list50.map(s => s.quotes.KRW.percent_change_6h); // 6시간 간격 등락폭
    const change24H = list50.map(s => s.quotes.KRW.percent_change_24h); // 24시간 간격 등락폭
     const data = {
          labels: labels,
          datasets: [
             {
                label: '베타 계수',
                data: betas,
                type: 'line'
             },
            {
                label: '등락폭(1시간)',
                data: change1H,
                type: 'line'
            },
            {
                label: '등락폭(6시간)',
                data: change6H,
                type: 'line'
            },
            {
                label: '등락폭(24시간)',
                data: change24H,
                type: 'line'
            }
        ],
     };

     const config = {
           data: data,
           options: {
             scales: {
               y: {
                 beginAtZero: true
               }
             }
           },
         };

     const ctx = document.getElementById('upbit_cart');
     new Chart(ctx, config);
});