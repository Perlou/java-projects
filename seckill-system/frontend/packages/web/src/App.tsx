import { useState, useEffect, useCallback } from "react";
import type { SeckillGoods } from "./types";
import { api } from "./services/api";
import { GoodsCard } from "./components/GoodsCard";
import "./App.css";

function App() {
  const [goods, setGoods] = useState<SeckillGoods[]>([]);
  const [userId, setUserId] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadGoods = useCallback(async () => {
    try {
      const data = await api.getGoodsList();
      setGoods(data);
      setError("");
    } catch (err) {
      setError("无法连接服务器，请检查后端是否启动");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadGoods();
    // 每5秒刷新一次
    const interval = setInterval(loadGoods, 5000);
    return () => clearInterval(interval);
  }, [loadGoods]);

  const handleRefresh = () => {
    setLoading(true);
    loadGoods();
  };

  return (
    <div className="app">
      {/* 背景动画 */}
      <div className="bg-animation">
        <div className="bg-circle bg-circle-1"></div>
        <div className="bg-circle bg-circle-2"></div>
        <div className="bg-circle bg-circle-3"></div>
      </div>

      <div className="container">
        {/* 头部 */}
        <header className="header">
          <div className="header-content">
            <h1 className="title">
              <span className="title-icon">🔥</span>
              限时秒杀
            </h1>
            <p className="subtitle">
              Phase 12 实战项目 · Redis + RabbitMQ + 分布式锁
            </p>
          </div>

          <div className="header-stats">
            <div className="stat-item">
              <span className="stat-value">{goods.length}</span>
              <span className="stat-label">热门商品</span>
            </div>
            <div className="stat-item">
              <span className="stat-value">
                {goods.reduce((acc, g) => acc + g.stockCount, 0)}
              </span>
              <span className="stat-label">剩余库存</span>
            </div>
          </div>
        </header>

        {/* 用户设置 */}
        <div className="user-panel">
          <div className="user-input-group">
            <label>用户 ID</label>
            <input
              type="number"
              value={userId}
              onChange={(e) => setUserId(Number(e.target.value) || 1)}
              min={1}
            />
          </div>
          <p className="tip">💡 使用不同的用户 ID 模拟多用户秒杀场景</p>
          <button className="btn-refresh" onClick={handleRefresh}>
            🔄 刷新商品
          </button>
        </div>

        {/* 商品列表 */}
        {loading ? (
          <div className="loading-state">
            <div className="loading-spinner-large"></div>
            <p>加载中...</p>
          </div>
        ) : error ? (
          <div className="error-state">
            <span className="error-icon">⚠️</span>
            <p>{error}</p>
            <button className="btn-retry" onClick={handleRefresh}>
              重试
            </button>
          </div>
        ) : goods.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📦</span>
            <p>暂无秒杀商品</p>
          </div>
        ) : (
          <div className="goods-grid">
            {goods.map((item) => (
              <GoodsCard
                key={item.id}
                goods={item}
                userId={userId}
                onSeckillComplete={loadGoods}
              />
            ))}
          </div>
        )}

        {/* 页脚 */}
        <footer className="footer">
          <p>
            🚀 技术栈: React + TypeScript + Vite | Spring Boot + Redis +
            RabbitMQ
          </p>
          <p className="footer-links">
            <a href="/swagger-ui.html" target="_blank">
              API 文档
            </a>
            <span className="divider">·</span>
            <a href="https://github.com" target="_blank">
              GitHub
            </a>
          </p>
        </footer>
      </div>
    </div>
  );
}

export default App;
