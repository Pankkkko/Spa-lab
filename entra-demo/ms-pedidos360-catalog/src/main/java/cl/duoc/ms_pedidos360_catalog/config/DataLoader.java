package cl.duoc.ms_pedidos360_catalog.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.duoc.ms_pedidos360_catalog.models.Producto;
import cl.duoc.ms_pedidos360_catalog.repository.ProductoRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository repository;

    public DataLoader(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {

        // Evita duplicar los productos cada vez que inicia el micro
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(

            // =====================================================
            // FUSILES
            // =====================================================

            producto(
                "FUS-001",
                "Izhmash AKM",
                "Legendario Fusil de asalto sovietico, en calibre 7.62x39mm",
                "Fusiles",
                "189990",
                12
            ),

            producto(
                "FUS-002",
                "Colt M4A1",
                "Fusil de asalto basado en la plataforma AR15, calibre 5.56x45mm (tambien acepta .223 Remington)",
                "Fusiles",
                "219990",
                8
            ),

            producto(
                "Colt M4A1 SOPMOD",
                "Raven M4-X",
                "Replica ficticia de estilo tactico para airsoft.",
                "Fusiles",
                "249990",
                15
            ),

            producto(
                "FUS-004",
                "Titan SR-5",
                "Replica ficticia de coleccion con cuerpo metalico.",
                "Fusiles",
                "299990",
                6
            ),

            producto(
                "FUS-005",
                "Specter MK-II",
                "Replica ficticia de estilo futurista.",
                "Fusiles",
                "279990",
                10
            ),

            // =====================================================
            // ESCOPETAS
            // =====================================================

            producto(
                "ESC-001",
                "Thunder-12",
                "Replica ficticia de escopeta para airsoft.",
                "Escopetas",
                "149990",
                9
            ),

            producto(
                "ESC-002",
                "Stormbreaker",
                "Replica deportiva ficticia de estilo clasico.",
                "Escopetas",
                "169990",
                7
            ),

            producto(
                "ESC-003",
                "Blackhawk SG",
                "Replica ficticia con acabado mate.",
                "Escopetas",
                "139990",
                14
            ),

            producto(
                "ESC-004",
                "Raptor Tactical",
                "Replica ficticia para partidas de airsoft.",
                "Escopetas",
                "184990",
                5
            ),

            producto(
                "ESC-005",
                "Nova-12",
                "Replica ficticia de estilo futurista.",
                "Escopetas",
                "159990",
                11
            ),

            // =====================================================
            // CARABINAS
            // =====================================================

            producto(
                "CAR-001",
                "Falcon C1",
                "Replica compacta ficticia para airsoft.",
                "Carabinas",
                "129990",
                13
            ),

            producto(
                "CAR-002",
                "Warden CQB",
                "Replica ficticia compacta de estilo tactico.",
                "Carabinas",
                "154990",
                10
            ),

            producto(
                "CAR-003",
                "Ghost Carbine",
                "Replica ficticia ligera para partidas.",
                "Carabinas",
                "179990",
                8
            ),

            producto(
                "CAR-004",
                "Vector C-9",
                "Replica ficticia de diseno compacto.",
                "Carabinas",
                "194990",
                6
            ),

            producto(
                "CAR-005",
                "Orion Compact",
                "Replica ficticia para coleccion y airsoft.",
                "Carabinas",
                "164990",
                12
            ),

            // =====================================================
            // PISTOLAS
            // =====================================================

            producto(
                "PIS-001",
                "Viper-9 Mock",
                "Replica ficticia de pistola para airsoft.",
                "Pistolas",
                "89990",
                20
            ),

            producto(
                "PIS-002",
                "Falcon P-1",
                "Replica ficticia compacta con acabado negro.",
                "Pistolas",
                "94990",
                16
            ),

            producto(
                "PIS-003",
                "Specter-9",
                "Replica deportiva ficticia para airsoft.",
                "Pistolas",
                "109990",
                18
            ),

            producto(
                "PIS-004",
                "Nova Sidearm",
                "Replica ficticia ligera de estilo moderno.",
                "Pistolas",
                "84990",
                22
            ),

            producto(
                "PIS-005",
                "Raven P-X",
                "Replica ficticia de coleccion.",
                "Pistolas",
                "119990",
                9
            ),

            // =====================================================
            // REVOLVERES
            // =====================================================

            producto(
                "REV-001",
                "Old Town R1",
                "Replica ficticia de estilo western para coleccion.",
                "Revolveres",
                "99990",
                7
            ),

            producto(
                "REV-002",
                "Silver Ghost",
                "Replica ficticia con acabado metalizado.",
                "Revolveres",
                "124990",
                5
            ),

            producto(
                "REV-003",
                "Frontier X",
                "Replica ficticia inspirada en diseños clasicos.",
                "Revolveres",
                "114990",
                8
            ),

            producto(
                "REV-004",
                "Ranger Mock",
                "Replica ficticia para airsoft y cosplay.",
                "Revolveres",
                "104990",
                10
            ),

            producto(
                "REV-005",
                "Dustland R2",
                "Replica ficticia de estilo western.",
                "Revolveres",
                "129990",
                4
            ),

            // =====================================================
            // CARGADORES
            // =====================================================

            producto(
                "CAR-006",
                "Mag-X Standard",
                "Accesorio ficticio de utileria para replicas.",
                "Cargadores",
                "24990",
                30
            ),

            producto(
                "CAR-007",
                "Mag-X Extended",
                "Accesorio ficticio de mayor capacidad visual.",
                "Cargadores",
                "29990",
                25
            ),

            producto(
                "CAR-008",
                "Tactical Mag Mock",
                "Cargador ficticio para replicas de airsoft.",
                "Cargadores",
                "19990",
                35
            ),

            producto(
                "CAR-009",
                "Raven Mag",
                "Accesorio ficticio compatible con replicas.",
                "Cargadores",
                "27990",
                18
            ),

            producto(
                "CAR-010",
                "Vortex Mag",
                "Cargador ficticio de coleccion.",
                "Cargadores",
                "22990",
                27
            ),

            // =====================================================
            // MUNICION
            // =====================================================

            producto(
                "MUN-001",
                "BB Mock Standard",
                "Municion ficticia de utileria para pruebas del catalogo.",
                "Municion",
                "8990",
                50
            ),

            producto(
                "MUN-002",
                "BB Mock Premium",
                "Producto ficticio para representar consumibles.",
                "Municion",
                "11990",
                42
            ),

            producto(
                "MUN-003",
                "Training BB Pack",
                "Producto ficticio de entrenamiento para el catalogo.",
                "Municion",
                "9990",
                60
            ),

            producto(
                "MUN-004",
                "Eco BB Mock",
                "Producto ficticio de utileria para pruebas.",
                "Municion",
                "12990",
                38
            ),

            producto(
                "MUN-005",
                "Match BB Mock",
                "Producto ficticio para representar municion deportiva.",
                "Municion",
                "10990",
                45
            ),

            // =====================================================
            // ACCESORIOS
            // =====================================================

            producto(
                "ACC-001",
                "Red Dot Mock",
                "Accesorio ficticio de utileria para replicas.",
                "Accesorios",
                "44990",
                17
            ),

            producto(
                "ACC-002",
                "Tactical Grip Mock",
                "Accesorio ficticio para personalizacion visual.",
                "Accesorios",
                "29990",
                21
            ),

            producto(
                "ACC-003",
                "Rail Cover Mock",
                "Accesorio ficticio para pruebas de catalogo.",
                "Accesorios",
                "15990",
                28
            ),

            producto(
                "ACC-004",
                "Carry Case Mock",
                "Estuche ficticio para almacenamiento y transporte.",
                "Accesorios",
                "54990",
                13
            ),

            producto(
                "ACC-005",
                "Utility Sling Mock",
                "Accesorio ficticio de transporte para replicas.",
                "Accesorios",
                "24990",
                24
            )
        ));

        System.out.println(
            "=============================================="
        );

        System.out.println(
            "DataLoader: 40 productos cargados correctamente."
        );

        System.out.println(
            "=============================================="
        );
    }

    private Producto producto(
            String sku,
            String nombre,
            String descripcion,
            String categoria,
            String precio,
            Integer stock
    ) {

        return Producto.builder()
                .sku(sku)
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .precio(new BigDecimal(precio))
                .imagenes(
                    List.of(
                        "https://placehold.co/600x600?text="
                            + sku
                    )
                )
                .stock(stock)
                .build();
    }
}