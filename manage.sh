#!/bin/bash
# ============================================================
# nopCommerce Local – Startup Script
# Dùng để start/stop/reset toàn bộ môi trường
# ============================================================

ACTION=${1:-"start"}

case $ACTION in
    start)
        echo "🚀 Starting nopCommerce local environment..."
        docker-compose up -d
        echo ""
        echo "⏳ Waiting for services to be ready..."
        sleep 20
        echo ""
        echo "✅ Services started!"
        echo "   📌 nopCommerce : http://localhost:8080"
        echo "   📌 Selenium Grid: http://localhost:4444"
        echo "   📌 SQL Server  : localhost:1433"
        echo "   📌 SA Password : Test@123456!"
        echo ""
        echo "📋 First time? Open http://localhost:8080 to run Installation Wizard"
        ;;

    stop)
        echo "🛑 Stopping nopCommerce local environment..."
        docker-compose stop
        echo "✅ Stopped (data preserved)"
        ;;

    reset)
        echo "⚠️  RESET: This will delete ALL data!"
        read -p "Are you sure? (y/N): " confirm
        if [[ $confirm == "y" || $confirm == "Y" ]]; then
            docker-compose down -v
            rm -rf volumes/nopcommerce_data/*
            rm -rf volumes/mssql_data/*
            echo "✅ Reset complete. Run './manage.sh start' to begin fresh."
        else
            echo "❌ Reset cancelled."
        fi
        ;;

    status)
        echo "📊 Container Status:"
        docker-compose ps
        echo ""
        echo "📊 Health Check:"
        docker inspect nop_sqlserver --format='SQL Server: {{.State.Health.Status}}' 2>/dev/null || echo "SQL Server: Not running"
        ;;

    logs)
        SERVICE=${2:-""}
        docker-compose logs -f $SERVICE
        ;;

    *)
        echo "Usage: ./manage.sh [start|stop|reset|status|logs]"
        ;;
esac
