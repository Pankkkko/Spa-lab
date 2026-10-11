type CatalogoEstadoProps =
    | {
          tipo: 'cargando';
      }
    | {
          tipo: 'error';
          mensaje: string;
          onReintentar: () => void;
      }
    | {
          tipo: 'vacio';
      };

export default function CatalogoEstado(
    props: CatalogoEstadoProps,
) {
    if (props.tipo === 'cargando') {
        return (
            <div className="catalogo-loading">
                <div className="catalogo-spinner"></div>
                <p>Cargando catálogo...</p>
            </div>
        );
    }

    if (props.tipo === 'error') {
        return (
            <div className="catalogo-error">
                <div className="catalogo-error-icon">!</div>

                <h2>No pudimos cargar el catálogo</h2>
                <p>{props.mensaje}</p>

                <button
                    type="button"
                    onClick={props.onReintentar}
                >
                    Reintentar
                </button>
            </div>
        );
    }

    return (
        <div className="sin-resultados">
            <div className="sin-resultados-icon">◌</div>

            <h3>No encontramos productos</h3>

            <p>
                Prueba con otra categoría o término de búsqueda.
            </p>
        </div>
    );
}