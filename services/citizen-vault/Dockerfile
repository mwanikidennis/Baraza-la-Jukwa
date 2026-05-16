FROM python:3.14-slim AS builder
WORKDIR /app
COPY requirements.txt .
RUN pip install --user --no-cache-dir -r requirements.txt

FROM python:3.14-slim
WORKDIR /app

# Create non-root user
RUN groupadd -r python && useradd -r -g python python

# Copy dependencies from builder
COPY --from=builder /root/.local /home/python/.local
ENV PATH=/home/python/.local/bin:$PATH
ENV PYTHONUNBUFFERED=1

COPY src/ .

RUN chown -R python:python /app
USER python

ENV PORT=3011
EXPOSE 3011

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:3011/health').read()" || exit 1

CMD ["python", "main.py"]
