import { useState } from "react";
import type { SeckillGoods, SeckillResult } from "../types";
import { api } from "../services/api";
import "./GoodsCard.css";

interface GoodsCardProps {
  goods: SeckillGoods;
  userId: number;
  onSeckillComplete: () => void;
}

const productIcons: Record<string, string> = {
  iPhone: "📱",
  MacBook: "💻",
  AirPods: "🎧",
  Watch: "⌚",
  iPad: "📲",
};

export function GoodsCard({
  goods,
  userId,
  onSeckillComplete,
}: GoodsCardProps) {
  const [status, setStatus] = useState<SeckillResult>("PENDING");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const icon =
    Object.entries(productIcons).find(([key]) =>
      goods.goodsName.includes(key)
    )?.[1] || "🛍️";

  const isSoldOut = goods.stockCount <= 0;
  const discount = Math.round(
    (1 - goods.seckillPrice / goods.originalPrice) * 100
  );

  const handleSeckill = async () => {
    if (loading || isSoldOut || status === "SUCCESS") return;

    setLoading(true);
    setMessage("");

    try {
      const result = await api.doSeckill(userId, goods.id);

      if (result.code === 200) {
        setStatus("QUEUING");
        setMessage("秒杀成功，正在处理订单...");
        pollResult();
      } else {
        setStatus("FAIL");
        setMessage(result.message);
      }
    } catch (error) {
      setStatus("FAIL");
      setMessage("网络错误，请重试");
    } finally {
      setLoading(false);
    }
  };

  const pollResult = async () => {
    let attempts = 0;
    const maxAttempts = 10;

    const poll = async () => {
      try {
        const result = await api.getSeckillResult(userId, goods.id);

        if (result.code === 200 && result.data) {
          setStatus("SUCCESS");
          setMessage("🎉 订单创建成功！");
          onSeckillComplete();
          return;
        }

        if (result.code !== 200 && !result.message.includes("排队")) {
          setStatus("FAIL");
          setMessage(result.message);
          onSeckillComplete();
          return;
        }

        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 1000);
        }
      } catch {
        // 继续轮询
        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 1000);
        }
      }
    };

    setTimeout(poll, 500);
  };

  const getButtonText = () => {
    if (loading) return "抢购中...";
    if (status === "SUCCESS") return "已抢到";
    if (status === "QUEUING") return "排队中...";
    if (isSoldOut) return "已售罄";
    return "立即抢购";
  };

  const getButtonClass = () => {
    if (status === "SUCCESS") return "btn-success";
    if (status === "QUEUING" || loading) return "btn-loading";
    if (isSoldOut) return "btn-disabled";
    return "btn-active";
  };

  return (
    <div className="goods-card">
      <div className="goods-badge">{discount}% OFF</div>

      <div className="goods-image">
        <span className="goods-icon">{icon}</span>
      </div>

      <div className="goods-content">
        <h3 className="goods-name">{goods.goodsName}</h3>

        <div className="goods-price">
          <span className="seckill-price">
            ¥{goods.seckillPrice.toFixed(2)}
          </span>
          <span className="original-price">
            ¥{goods.originalPrice.toFixed(2)}
          </span>
        </div>

        <div className="goods-stock">
          <div className="stock-bar">
            <div
              className="stock-progress"
              style={{ width: `${Math.min(goods.stockCount, 100)}%` }}
            />
          </div>
          <span className="stock-text">
            剩余 <strong>{goods.stockCount}</strong> 件
          </span>
        </div>

        <button
          className={`btn-seckill ${getButtonClass()}`}
          onClick={handleSeckill}
          disabled={loading || isSoldOut || status === "SUCCESS"}
        >
          {loading && <span className="loading-spinner" />}
          {getButtonText()}
        </button>

        {message && (
          <div className={`message ${status === "FAIL" ? "error" : "success"}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}
