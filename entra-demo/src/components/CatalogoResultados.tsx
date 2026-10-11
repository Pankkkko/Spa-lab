interface CatalogoResultadosProps {
    categoriaSeleccionada: string;
    cantidadProductos: number;
}

export default function CatalogoResultados({
    categoriaSeleccionada,
    cantidadProductos,
}: CatalogoResultadosProps) {
    return (
        <div className="resultados-header">
            <div>
                <span className="resultados-label">
                    CATÁLOGO
                </span>

                <h2>
                    {categoriaSeleccionada === 'Todos'
                        ? 'Todos los productos'
                        : categoriaSeleccionada}
                </h2>
            </div>

            <span className="contador-productos">
                {cantidadProductos} productos
            </span>
        </div>
    );
}