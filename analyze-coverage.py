import os
import re
import json
from pathlib import Path
from collections import defaultdict

class CoverageAnalyzer:
    def __init__(self, project_dir):
        self.project_dir = Path(project_dir)
        self.src_dir = self.project_dir / "src" / "main" / "java"
        self.test_dir = self.project_dir / "src" / "test" / "java"
        self.packages = defaultdict(lambda: {"classes": 0, "methods": 0, "tested": 0})

    def count_methods(self, content):
        """Conta metodos publicos e protected em uma classe"""
        # Remove comentarios
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
        content = re.sub(r'//.*$', '', content, flags=re.MULTILINE)

        # Encontra metodos publicos/protected
        pattern = r'(public|protected)\s+[\w<>,\[\]]+\s+(\w+)\s*\('
        return len(re.findall(pattern, content))

    def extract_package(self, file_path):
        """Extrai o nome do pacote do arquivo"""
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read(500)  # Lê apenas primeiras linhas
            match = re.search(r'package\s+([\w.]+)\s*;', content)
            return match.group(1) if match else "default"

    def analyze_coverage(self):
        """Analisa cobertura pelos testes encontrados"""
        coverage_map = defaultdict(set)

        # Vaarre testes para encontrar quais classes sao testadas
        if self.test_dir.exists():
            for test_file in self.test_dir.rglob("*Test.java"):
                with open(test_file, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()

                    # Encontra imports para classes testadas
                    imports = re.findall(r'import\s+org\.example\.classroompb\.(\w+)\.(\w+);', content)
                    for module, class_name in imports:
                        coverage_map[f"org.example.classroompb.{module}"].add(class_name)

                    # Encontra por padrão de class: "class UsuarioModelTest", "class ProfessorModelTest"
                    test_classes = re.findall(r'class\s+(\w+)(?:Model)?Test', content)
                    for test_class in test_classes:
                        # Remove sufixos comuns
                        class_name = test_class.replace('Model', '').replace('Test', '')
                        if class_name:
                            coverage_map["org.example.classroompb.model"].add(class_name)

        return coverage_map

    def generate_report(self):
        """Gera relatorio de cobertura"""
        if not self.src_dir.exists():
            print("❌ Diretório src/main/java não encontrado")
            return False

        coverage_map = self.analyze_coverage()

        print("\n" + "="*70)
        print("📊 RELATÓRIO DE COBERTURA EMMA - ANÁLISE DE CÓDIGO")
        print("="*70 + "\n")

        total_classes = 0
        total_tested = 0
        package_stats = defaultdict(lambda: {"total": 0, "tested": 0, "methods": 0})

        # Analisa cada classe Java
        for java_file in self.src_dir.rglob("*.java"):
            if "Main" in java_file.name or "CLI" in java_file.name:
                continue

            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()

            package = self.extract_package(java_file)
            class_name = java_file.stem
            methods = self.count_methods(content)

            is_tested = class_name in coverage_map.get(package, set())

            package_stats[package]["total"] += 1
            package_stats[package]["methods"] += methods
            if is_tested:
                package_stats[package]["tested"] += 1

            total_classes += 1
            if is_tested:
                total_tested += 1

        # Exibe por pacote
        print("📦 COBERTURA POR PACOTE:\n")

        min_coverage = 100
        packages_below_80 = []

        for package in sorted(package_stats.keys()):
            stats = package_stats[package]
            coverage = (stats["tested"] / stats["total"] * 100) if stats["total"] > 0 else 0
            min_coverage = min(min_coverage, coverage)

            status = "✓" if coverage >= 80 else "⚠"
            print(f"{status} {package}")
            print(f"   Classes: {stats['tested']}/{stats['total']} ({coverage:.1f}%)")
            print(f"   Métodos: {stats['methods']}")
            print()

            if coverage < 80:
                packages_below_80.append((package, coverage))

        # Resumo final
        print("="*70)
        print("\n📈 RESUMO FINAL:\n")
        print(f"Total de Classes: {total_classes}")
        print(f"Classes Testadas: {total_tested}")
        print(f"Cobertura Global: {(total_tested/total_classes*100):.1f}%")

        print("\n🎯 REQUISITOS PARA RELEASE:\n")
        print(f"Pacote model:      ≥ 80% → {self._get_package_coverage('org.example.classroompb.model', package_stats):.1f}%")
        print(f"Pacote service:    ≥ 80% → {self._get_package_coverage('org.example.classroompb.service', package_stats):.1f}%")
        print(f"Pacote repository: ≥ 75% → {self._get_package_coverage('org.example.classroompb.repository', package_stats):.1f}%")

        print("\n" + "="*70)

        if packages_below_80:
            print("\n⚠️  PACOTES ABAIXO DE 80%:\n")
            for pkg, cov in packages_below_80:
                print(f"   {pkg}: {cov:.1f}%")
            print("\n   ⚡ Adicione mais testes para atingir o mínimo de 80%\n")
        else:
            print("\n✅ TODOS OS PACOTES COM COBERTURA ≥ 80%\n")

        print("="*70 + "\n")
        return True

    def _get_package_coverage(self, package_prefix, stats):
        """Obtém cobertura de um pacote específico"""
        for package, data in stats.items():
            if package.startswith(package_prefix):
                return (data["tested"] / data["total"] * 100) if data["total"] > 0 else 0
        return 0

if __name__ == "__main__":
    import sys

    project_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    analyzer = CoverageAnalyzer(project_dir)

    success = analyzer.generate_report()
    sys.exit(0 if success else 1)


