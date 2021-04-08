from website_sources import create_app
import os

app = create_app()

# main
# start aplikacji, konfiguracja adresu URL i portu
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5090)
