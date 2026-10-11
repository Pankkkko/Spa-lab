interface CatalogoFiltrosProps {
    busqueda: string;
    onBusquedaChange: (valor: string) => void;
    categoriaSeleccionada: string;
    onCategoriaChange: (categoria: string) => void;
}

const CATEGORIAS = [
    'Todos',
    'Accesorios',
    'Municion',
    'Cargadores',
    'Revolveres',
    'Pistolas',
    'Carabinas',
    'Escopetas',
    'Fusiles',
];

export default function CatalogoFiltros({
    busqueda,
    onBusquedaChange,
    categoriaSeleccionada,
    onCategoriaChange,
}: CatalogoFiltrosProps) {
    return (
        <section className="catalogo-filtros">
            <div className="catalogo-search">
                <span className="search-icon">⌕</span>

                <input
                    type="text"
                    placeholder="Buscar por nombre, SKU o descripción..."
                    value={busqueda}
                    onChange={(e) => onBusquedaChange(e.target.value)}
                />

                {busqueda && (
                    <button
                        className="search-clear"
                        type="button"
                        onClick={() => onBusquedaChange('')}
                        aria-label="Limpiar búsqueda"
                    >
                        ×
                    </button>
                )}
            </div>

            <div className="categorias">
                {CATEGORIAS.map((categoria) => (
                    <button
                        key={categoria}
                        type="button"
                        className={
                            categoriaSeleccionada === categoria
                                ? 'categoria-btn activa'
                                : 'categoria-btn'
                        }
                        onClick={() => onCategoriaChange(categoria)}
                    >
                        {categoria}
                    </button>
                ))}
            </div>
        </section>
    );
}