#!/usr/bin/env python3
# Humo de la imagen recien desplegada (MJ-15 del plan de mejoras): espera a que /actuator/health responda UP, ingresa
# como sysadmin y llama a la herramienta MCP "eco" con el token del seed. La version la verifica deploy/smoke.py, en
# el paso siguiente del job.
# Lo usa el job "imagen" de .github/workflows/calidad.yml despues de `python deploy/deploy.py --local local shiro-be --seed`.
# Uso: python .github/scripts/smoke_imagen.py [--url http://localhost:7090/api]
import argparse
import json
import sys
import time
import urllib.error
import urllib.request

# Credenciales del entorno local de deploy.yml y del seed (2026-10-09-insert-tokens-seed.yaml): no son secretos
USUARIO = "sysadmin"
CONTRASENA = "123"
TOKEN_DEL_SEED = "sysadmin-access-token"


def pedir(url: str, cuerpo: dict | None = None, encabezados: dict | None = None) -> tuple[int, str]:
    """POST (con cuerpo) o GET; responde el status y el texto de la respuesta."""
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    pedido = urllib.request.Request(url, data=datos, method="POST" if datos else "GET")
    pedido.add_header("Content-Type", "application/json")
    for clave, valor in (encabezados or {}).items():
        pedido.add_header(clave, valor)
    try:
        with urllib.request.urlopen(pedido, timeout=20) as respuesta:
            return respuesta.status, respuesta.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode()


def esperar_salud(base: str, segundos: int) -> None:
    """Espera hasta que el health responda UP o corta con error."""
    limite = time.time() + segundos
    while time.time() < limite:
        try:
            status, texto = pedir(f"{base}/actuator/health")
            if status == 200 and '"UP"' in texto:
                print("[imagen] health UP")
                return
        except OSError:
            pass
        time.sleep(5)
    sys.exit(f"[imagen] el backend no respondio UP en {segundos} s")


def json_de_mcp(texto: str) -> dict:
    """La respuesta de MCP llega como JSON o como evento SSE (lineas data:)."""
    for linea in texto.splitlines():
        if linea.startswith("data:"):
            return json.loads(linea[5:].strip())
    return json.loads(texto)


def main() -> int:
    parser = argparse.ArgumentParser(description="Humo de la imagen: health, version, login y eco por MCP.")
    parser.add_argument("--url", default="http://localhost:7090/api", help="URL base de la API")
    parser.add_argument("--espera", type=int, default=300, help="segundos maximos para que levante")
    args = parser.parse_args()

    esperar_salud(args.url, args.espera)
    status, texto = pedir(f"{args.url}/usuario/iniciar-sesion", {"nombreDeUsuario": USUARIO, "contrasena": CONTRASENA})
    if status != 200:
        sys.exit(f"[imagen] el ingreso de {USUARIO} respondio {status}")
    print("[imagen] ingreso de sysadmin OK")

    mcp = {"Authorization": f"Bearer {TOKEN_DEL_SEED}", "Accept": "application/json, text/event-stream"}
    status, texto = pedir(f"{args.url}/mcp", {"jsonrpc": "2.0", "id": 1, "method": "tools/call",
                                             "params": {"name": "eco", "arguments": {"texto": "hola shiro"}}}, mcp)
    if status != 200:
        sys.exit(f"[imagen] MCP respondio {status}")
    resultado = json_de_mcp(texto).get("result", {})
    textos = [c.get("text") for c in resultado.get("content", [])]
    if "hola shiro" not in textos:
        sys.exit(f"[imagen] eco no devolvio el texto: {texto[:300]}")
    print("[imagen] eco por MCP OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
