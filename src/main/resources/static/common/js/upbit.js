window.addEventListener("DOMContentLoaded", function() {
    const ws = new WebSocket("ws://localhost:3000/upbit");
    ws.onopen = function(e) {

        console.log("연결!", e);
    };

    ws.onclose = function(e) {
        console.log("종료!", e);
    };

    ws.onmessage = function(message) {
        const items = JSON.parse(message.data);

        const data = items.reduce((acc, cur) => {
            acc[cur.market] = acc[cur.market] || {};

            acc[cur.market].tradeDate = cur.trade_date;
            acc[cur.market].tradeTime = cur.trade_time;
            acc[cur.market].accTradePrice = cur.acc_trade_price;
            acc[cur.market].openingPrice = cur.opening_price;
            acc[cur.market].tradePrice = cur.trade_price;
            acc[cur.market].tradeVolume = cur.trade_volume;
            acc[cur.market].highPrice = cur.high_price;
            acc[cur.market].lowPrice = cur.low_price;
            acc[cur.market].changePrice = cur.change_price;
            return acc;
        }, {});
        console.log(data);
    };
});